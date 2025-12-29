package com.game.subwayworkerserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpsItemsCreator {

    private final CoworkerDataCreator coworkerDataCreator;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String COWORKER_LOCATIONS_KEY = "coworker:locations";
    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final String COWORKER_DATA_PREFIX = "coworker:data:";
    private static final String VISITED_GRID_KEY = "map:visited:grids";
    private static final String GRID_LOCK_PREFIX = "lock:grid:";

    private static final long MAP_DATA_TTL_HOURS = 3;
    private static final double GRID_SIZE = 0.01;
    private static final int COWORKER_COUNT = 4;
    private static final int GIFTS_COUNT = 2;
    private static final double MAX_JITTER = 0.004;

    public void createItemsInArea(final double latitude, final double longitude) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                double targetLat = latitude + (i * GRID_SIZE);
                double targetLon = longitude + (j * GRID_SIZE);

                String gridId = String.format("%.2f:%.2f",
                        Math.floor(targetLat / GRID_SIZE) * GRID_SIZE,
                        Math.floor(targetLon / GRID_SIZE) * GRID_SIZE);

                if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(VISITED_GRID_KEY, gridId))) {
                    continue;
                }

                String lockKey = GRID_LOCK_PREFIX + gridId;
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(acquired)) {
                    try {
                        if (Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(VISITED_GRID_KEY, gridId))) {
                            log.info("--- [New Area] 구역 생성 시작: {} ---", gridId);
                            generateDataWithPipeline(targetLat, targetLon, gridId);
                        }
                    } finally {
                        redisTemplate.delete(lockKey);
                    }
                }
            }
        }
    }

    private void generateDataWithPipeline(double lat, double lon, String gridId) {
        Map<String, Point> coworkers = new HashMap<>();
        Map<String, Point> gifts = new HashMap<>();
        List<Map<String, String>> hashDetails = new ArrayList<>();

        createLocations(lat, lon, COWORKER_COUNT, "COWORKER", coworkers, hashDetails);
        createLocations(lat, lon, GIFTS_COUNT, "GIFT", gifts, null);

        redisTemplate.executePipelined((RedisConnection connection) -> {
            if (!coworkers.isEmpty()) redisTemplate.opsForGeo().add(COWORKER_LOCATIONS_KEY, coworkers);
            if (!gifts.isEmpty()) redisTemplate.opsForGeo().add(RANDOM_GIFT_BOX_KEY, gifts);

            for (Map<String, String> data : hashDetails) {
                String key = COWORKER_DATA_PREFIX + data.get("id");
                redisTemplate.opsForHash().putAll(key, data);
                redisTemplate.expire(key, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
            }

            redisTemplate.opsForSet().add(VISITED_GRID_KEY, gridId);
            redisTemplate.expire(VISITED_GRID_KEY, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
            return null;
        });
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
