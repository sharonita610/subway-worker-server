package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.GpsRequest;
import com.game.subwayworkerserver.controller.response.GpsRandomGiftResponse;
import com.game.subwayworkerserver.controller.response.ItemLocationResponse;
import com.game.subwayworkerserver.controller.response.RandomGpsResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.game.subwayworkerserver.controller.request.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class GpsService {

    // RedisTemplate은 String, String 타입을 사용한다고 가정합니다.
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Key 상수 정의 (게임 아이템, 동료, 덫 등)
    private static final String FRIEND_LOCATIONS_KEY = "friend:locations";
    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:"; // 획득 기록 접두사
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
                .sort(Sort.Direction.ASC) // 거리 순으로 정렬
                .limit(MAX_ITEMS_TO_RETURN); // <--- 지도에 표시할 최대 개수 제한!

        // 3. 동료 위치 조회 (FRIEND_LOCATIONS_KEY 사용)
        List<ItemLocationResponse> friends = findLocationsByGeoKey(FRIEND_LOCATIONS_KEY, searchArea, args, "friend", userUuid);

        // 4. 선물상자 위치 조회 (RANDOM_GIFT_BOX_KEY 사용)
        List<ItemLocationResponse> giftBoxes = findLocationsByGeoKey(RANDOM_GIFT_BOX_KEY, searchArea, args, "giftbox", userUuid);

        return new RandomGpsResponse(friends, giftBoxes);
    }

    /**
     * Redis Key를 기반으로 GeoSpatial 검색을 수행하는 헬퍼 메서드.
     */
    private List<ItemLocationResponse> findLocationsByGeoKey(
            String geoKey,
            Circle searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs args,
            String entityType,
            String userUuid
    ) {
        // Redis GEOSEARCH 실행 (4인수 radius 시그니처 사용 가정)
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        geoKey,
                        searchArea,
                        args
                );

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        if (userUuid != null) {
            // 획득 기록 키 생성 (예: user:acquired:UUID:friend:locations 또는 user:acquired:UUID:giftbox:locations)
            String acquiredKey = USER_ACQUIRED_PREFIX + userUuid + ":" + geoKey;
            Set<String> acquiredIds = redisTemplate.opsForSet().members(acquiredKey);
            Set<String> finalAcquiredIds = (acquiredIds != null) ? acquiredIds : Collections.emptySet();
            // 획득 기록에 없는 아이템만 필터링하여 매핑
            return results.getContent().stream()
                    .filter(result -> {
                        String itemId = result.getContent().getName();
                        return !finalAcquiredIds.contains(itemId);
                    })
                    .map(result -> mapToItemLocationResponse(result, entityType))
                    .toList();
        }

        return results.getContent().stream()
                .map(result -> mapToItemLocationResponse(result, entityType))
                .toList();
    }

    /**
     * GeoResult 객체를 ItemLocationResponse DTO로 매핑하는 헬퍼 메서드
     */
    private ItemLocationResponse mapToItemLocationResponse(
            GeoResult<RedisGeoCommands.GeoLocation<String>> result,
            String entityType
    ) {
        // 이 부분에서 실제 좌표 (GEOPOS) 및 상세 정보를 조회해야 합니다.
        // 현재는 임시 값 사용
        Double itemLongitude = result.getContent().getPoint().getX();
        Double itemLatitude = result.getContent().getPoint().getY();
        String itemFeature = entityType;
        String itemName = entityType.equals("friend") ? "동료" : "선물상자";

        return new ItemLocationResponse(
                itemName,
                itemLatitude,
                itemLongitude,
                itemFeature,
                result.getDistance().getValue()
        );
    }
}