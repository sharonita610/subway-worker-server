package com.game.subwayworkerserver.service.gps;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 사용자 주변 3x3 grid에 동료와 선물상자 위치를 생성한다.
 * grid 단위로 방문 여부를 기록하고, Redis 락으로 동시 요청의 중복 생성을 막는다.
 */
@Component
@RequiredArgsConstructor
public class GpsItemsCreator {

    private final CoworkerDataCreator coworkerDataCreator;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long MAP_DATA_TTL_HOURS = 1;
    private static final long GRID_LOCK_TTL_SECONDS = 10;
    private static final double GRID_SIZE = 0.01;
    private static final int COWORKER_COUNT = 2;
    private static final int GIFTS_COUNT = 2;
    private static final double MAX_JITTER = 0.004;

    private record Created(boolean coworkers, boolean gifts) {
    }

    public void createItemsInArea(final double latitude, final double longitude) {
        boolean anyCoworkersAdded = false;
        boolean anyGiftsAdded = false;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                double targetLat = latitude + (i * GRID_SIZE);
                double targetLon = longitude + (j * GRID_SIZE);
                String gridId = toGridId(targetLat, targetLon);

                if (isVisited(gridId)) {
                    continue;
                }

                String lockKey = GpsRedisKeys.GRID_LOCK_PREFIX + gridId;
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "LOCKED", GRID_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
                if (!Boolean.TRUE.equals(acquired)) {
                    continue;
                }

                try {
                    if (isVisited(gridId)) {
                        continue;
                    }
                    Created created = createGridItems(targetLat, targetLon, gridId);
                    anyCoworkersAdded |= created.coworkers();
                    anyGiftsAdded |= created.gifts();
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
        }

        if (anyCoworkersAdded) {
            redisTemplate.expire(GpsRedisKeys.COWORKER_LOCATIONS_KEY, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
        }
        if (anyGiftsAdded) {
            redisTemplate.expire(GpsRedisKeys.RANDOM_GIFT_BOX_KEY, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
        }
    }

    private Created createGridItems(double lat, double lon, String gridId) {
        Map<String, Point> coworkers = new HashMap<>();
        Map<String, Point> gifts = new HashMap<>();
        List<Map<String, String>> coworkerDetails = new ArrayList<>();

        createLocations(lat, lon, COWORKER_COUNT, "COWORKER", coworkers, coworkerDetails);
        createLocations(lat, lon, GIFTS_COUNT, "GIFT", gifts, null);

        addGeoIfNotEmpty(GpsRedisKeys.COWORKER_LOCATIONS_KEY, coworkers);
        addGeoIfNotEmpty(GpsRedisKeys.RANDOM_GIFT_BOX_KEY, gifts);

        for (Map<String, String> data : coworkerDetails) {
            String key = GpsRedisKeys.COWORKER_DATA_PREFIX + data.get("id");
            redisTemplate.opsForHash().putAll(key, data);
            redisTemplate.expire(key, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
        }

        redisTemplate.opsForSet().add(GpsRedisKeys.VISITED_GRID_KEY, gridId);
        redisTemplate.expire(GpsRedisKeys.VISITED_GRID_KEY, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);

        return new Created(!coworkers.isEmpty(), !gifts.isEmpty());
    }

    private boolean isVisited(String gridId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(GpsRedisKeys.VISITED_GRID_KEY, gridId));
    }

    private static String toGridId(double lat, double lon) {
        return String.format(Locale.ROOT, "%.2f:%.2f",
                Math.floor(lat / GRID_SIZE) * GRID_SIZE,
                Math.floor(lon / GRID_SIZE) * GRID_SIZE);
    }

    private void addGeoIfNotEmpty(String key, Map<String, Point> locations) {
        if (!locations.isEmpty()) {
            redisTemplate.opsForGeo().add(key, locations);
        }
    }

    private void createLocations(double lat, double lon, int count, String prefix,
                                 Map<String, Point> locations, List<Map<String, String>> hashList) {
        for (int i = 0; i < count; i++) {
            String id = prefix + "_" + UUID.randomUUID().toString().substring(0, 18);
            locations.put(id, new Point(generateRandomCoordinate(lon), generateRandomCoordinate(lat)));

            if (hashList != null) {
                Map<String, String> data = coworkerDataCreator.createRandomCoworkerAttribute();
                data.put("id", id);
                hashList.add(data);
            }
        }
    }

    private double generateRandomCoordinate(double center) {
        double offset = ThreadLocalRandom.current().nextDouble(-MAX_JITTER, MAX_JITTER);
        return Math.round((center + offset) * 1000000.0) / 1000000.0;
    }
}
