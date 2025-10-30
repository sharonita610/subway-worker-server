package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.GpsRequest;
import com.game.subwayworkerserver.controller.response.RandomGpsResponse;
import com.game.subwayworkerserver.controller.response.RandomGpsResponse.ItemLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.connection.RedisGeoCommands;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.game.subwayworkerserver.controller.response.RandomGpsResponse.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis Key 상수 정의 (게임 아이템, 동료, 덫 등)
    private static final String FRIEND_LOCATIONS_KEY = "friend:locations";
    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    private static final String FRIEND_DATA_PREFIX = "friend:data:"; // 👈 동료 인스턴스 속성 저장 키
    private static final int MAX_ITEMS_TO_RETURN = 10;

    /**
     * 1. 요청받은 좌표 기준 반경 내에 있는 아이템 리스트를 Redis GEOSEARCH를 통해 조회합니다.
     * (사용자가 맵에 접근할 때마다 호출되는 핵심 조회 API)
     */
    public RandomGpsResponse findItemsInArea(final GpsRequest request, final String userUuid) {

        // 1. 검색 영역 설정
        Circle searchArea = new Circle(
                new Point(request.longitude(), request.latitude()),
                new Distance(request.rangeKm(), RedisGeoCommands.DistanceUnit.KILOMETERS)
        );

        // 2. GeoRadiusCommandArgs 옵션 설정
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance() // 거리를 포함
                .includeCoordinates()
                .sort(Sort.Direction.ASC)
                .limit(MAX_ITEMS_TO_RETURN); // <--- 지도에 표시할 최대 개수 제한!

        // 3. 동료 위치 조회 및 필터링 (RandomFriendLocationResponse 반환)

        List<RandomFriendLocationResponse> friends = findFriendLocations(searchArea, args, userUuid);

        // 4. 선물상자 위치 조회 및 필터링 (ItemLocationResponse 반환)
        List<ItemLocationResponse> giftBoxes = findItemLocations(RANDOM_GIFT_BOX_KEY, searchArea, args, userUuid);

        return new RandomGpsResponse(friends, giftBoxes);
    }

    // --------------------------------------------------------------------------------------
    // --- Helper 1: 동료 조회 및 속성 매핑 (가장 복잡한 로직) ---
    // --------------------------------------------------------------------------------------

    private List<RandomFriendLocationResponse> findFriendLocations(
            Circle searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs args,
            String userUuid
    ) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo()
                        .radius(
                                FRIEND_LOCATIONS_KEY,
                                searchArea,
                                args
                        );

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> acquiredIds = getAcquiredIds(userUuid, FRIEND_LOCATIONS_KEY);

        return results.getContent().stream()
                .filter(result -> !acquiredIds.contains(result.getContent().getName())) // 획득하지 않은 동료만 통과
                .map(this::mapToRandomFriendLocationResponse)
                .toList();
    }

    private RandomFriendLocationResponse mapToRandomFriendLocationResponse(
            GeoResult<RedisGeoCommands.GeoLocation<String>> result
    ) {
        String friendId = result.getContent().getName();
        Double distance = result.getDistance().getValue();
        Point itemPoint = result.getContent().getPoint();

        // 1. Redis Hash에서 동료의 이름/레벨/직급 속성 로드
        String instanceKey = FRIEND_DATA_PREFIX + friendId;
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(instanceKey);

        // 데이터가 없으면 오류 처리 (Scheduler가 데이터를 생성해야 함)
        if (hashData.isEmpty()) {
            // throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "동료 속성 데이터가 Redis에 없습니다.");
            log.warn("Missing data for friend ID: {}", friendId);
            // 데이터가 없는 경우, 임시로 기본값 반환
            return RandomFriendLocationResponse.empty();
        }

        String friendName = (String) hashData.get("friendName");
        int friendLevel = Integer.parseInt((String) hashData.get("friendLevel"));
        String position = (String) hashData.get("position");

        return RandomFriendLocationResponse.of(
                friendId,
                friendName,
                friendLevel,
                position,
                itemPoint.getY(),
                itemPoint.getX(),
                distance
        );
    }

    private List<ItemLocationResponse> findItemLocations(
            String geoKey,
            Circle searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs args,
            String userUuid
    ) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(geoKey, searchArea, args);

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        // 선물 상자 획득 기록 필터링 (동료와 동일한 로직)
        Set<String> acquiredIds = getAcquiredIds(userUuid, geoKey);

        return results.getContent().stream()
                .filter(result -> !acquiredIds.contains(result.getContent().getName()))
                .map(this::mapToItemLocationResponse)
                .toList();
    }

    private ItemLocationResponse mapToItemLocationResponse(GeoResult<RedisGeoCommands.GeoLocation<String>> result) {
        Double itemLongitude = result.getContent().getPoint().getX();
        Double itemLatitude = result.getContent().getPoint().getY();

        return new ItemLocationResponse(
                "giftbox",
                itemLatitude,
                itemLongitude,
                result.getDistance().getValue()
        );
    }

    private Set<String> getAcquiredIds(String userUuid, String geoKey) {
        String acquiredKey = USER_ACQUIRED_PREFIX + userUuid + ":" + geoKey;
        Set<String> acquiredIds = redisTemplate.opsForSet().members(acquiredKey);
        return (acquiredIds != null) ? acquiredIds : java.util.Collections.emptySet();
    }
}
