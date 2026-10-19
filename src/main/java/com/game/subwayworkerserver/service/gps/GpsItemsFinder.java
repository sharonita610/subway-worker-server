package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse;
import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse.ItemLocationResponse;
import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse.RandomCoworkerLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 사용자 반경 1km 안의 동료와 선물상자를 조회한다.
 * 이미 획득한 항목과 데이터가 만료된 동료는 결과에서 제외하고, 만료된 동료는 위치 목록에서도 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GpsItemsFinder {

    private static final double SEARCH_RADIUS_KM = 1;
    private static final String GIFT_BOX_ITEM_NAME = "giftbox";

    private final RedisTemplate<String, String> redisTemplate;

    public RandomGpsResponse findItemsInArea(final double latitude, final double longitude, final String userUuid) {
        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(SEARCH_RADIUS_KM, RedisGeoCommands.DistanceUnit.KILOMETERS)
        );
        GeoRadiusCommandArgs args = GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sort(Sort.Direction.ASC);

        return new RandomGpsResponse(
                findCoworkers(searchArea, args, userUuid),
                findGiftBoxes(searchArea, args, userUuid)
        );
    }

    private List<RandomCoworkerLocationResponse> findCoworkers(Circle area, GeoRadiusCommandArgs args, String userUuid) {
        List<RandomCoworkerLocationResponse> found = new ArrayList<>();
        List<String> staleIds = new ArrayList<>();

        for (GeoResult<GeoLocation<String>> result : searchNotAcquired(GpsRedisKeys.COWORKER_LOCATIONS_KEY, area, args, userUuid)) {
            String coworkerId = result.getContent().getName();
            Map<Object, Object> hashData = redisTemplate.opsForHash().entries(GpsRedisKeys.COWORKER_DATA_PREFIX + coworkerId);
            RandomCoworkerLocationResponse response = toCoworkerResponse(coworkerId, hashData, result);
            if (response == null) {
                staleIds.add(coworkerId);
            } else {
                found.add(response);
            }
        }
        removeStaleCoworkers(staleIds);
        return found;
    }

    private List<ItemLocationResponse> findGiftBoxes(Circle area, GeoRadiusCommandArgs args, String userUuid) {
        return searchNotAcquired(GpsRedisKeys.RANDOM_GIFT_BOX_KEY, area, args, userUuid).stream()
                .map(result -> new ItemLocationResponse(
                        GIFT_BOX_ITEM_NAME,
                        result.getContent().getPoint().getY(),
                        result.getContent().getPoint().getX(),
                        result.getDistance().getValue()))
                .toList();
    }

    /** 반경 검색 결과에서 이 사용자가 이미 획득한 항목을 제외한다. */
    private List<GeoResult<GeoLocation<String>>> searchNotAcquired(
            String geoKey, Circle area, GeoRadiusCommandArgs args, String userUuid) {
        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().radius(geoKey, area, args);
        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> acquiredIds = getAcquiredIds(userUuid, geoKey);
        return results.getContent().stream()
                .filter(result -> !acquiredIds.contains(result.getContent().getName()))
                .toList();
    }

    /** 해시 데이터가 없거나 깨진 동료는 null을 돌려준다. 호출 측에서 위치 목록 정리 대상으로 삼는다. */
    private RandomCoworkerLocationResponse toCoworkerResponse(
            String coworkerId, Map<Object, Object> hashData, GeoResult<GeoLocation<String>> result) {
        String name = (String) hashData.get("coworkerName");
        String level = (String) hashData.get("coworkerLevel");
        if (name == null || level == null) {
            return null;
        }
        try {
            return RandomCoworkerLocationResponse.of(
                    coworkerId,
                    name,
                    Integer.parseInt(level),
                    (String) hashData.get("position"),
                    result.getContent().getPoint().getY(),
                    result.getContent().getPoint().getX(),
                    result.getDistance().getValue()
            );
        } catch (NumberFormatException e) {
            log.warn("[Gps] 동료 {}의 레벨 값이 올바르지 않습니다: {}", coworkerId, level);
            return null;
        }
    }

    private void removeStaleCoworkers(List<String> staleIds) {
        if (staleIds.isEmpty()) {
            return;
        }
        log.info("[Gps] 데이터가 만료된 동료 {}건을 위치 목록에서 제거합니다.", staleIds.size());
        redisTemplate.opsForGeo().remove(GpsRedisKeys.COWORKER_LOCATIONS_KEY, staleIds.toArray(new String[0]));
    }

    private Set<String> getAcquiredIds(String userUuid, String geoKey) {
        String acquiredKey = GpsRedisKeys.USER_ACQUIRED_PREFIX + userUuid + ":" + geoKey;
        Set<String> acquiredIds = redisTemplate.opsForSet().members(acquiredKey);
        return (acquiredIds != null) ? acquiredIds : Collections.emptySet();
    }
}
