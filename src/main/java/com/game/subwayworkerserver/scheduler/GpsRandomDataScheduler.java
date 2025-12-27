//package com.game.subwayworkerserver.scheduler;
//
//import com.game.subwayworkerserver.service.CoworkerDataCreator;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.core.Cursor;
//import org.springframework.data.redis.core.ScanOptions;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.stereotype.Component;
//
//import org.springframework.data.geo.Point;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class GpsRandomDataScheduler {
//
//    private final CoworkerDataCreator coworkerDataCreator;
//    private final RedisTemplate<String, String> redisTemplate;
//
//    // Redis Key 상수
//    private static final String COWORKER_LOCATIONS_KEY = "coworker:locations";
//    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
//    private static final String COWORKER_DATA_PREFIX = "coworker:data:";
//    private static final long MAP_DATA_TTL_HOURS = 3;
//
//    // 인천 부평구 (대략 37.48N, 126.72E) 및 서울 강서구 (대략 37.56N, 126.82E) 포함
//    private static final double MIN_LAT = 37.3; // 인천 남부(송도/연수) 포함
//    private static final double MAX_LAT = 37.7; // 서울 북부/고양시 일부 포함
//    private static final double MIN_LON = 126.5; // 인천 서해안 일부 포함
//    private static final double MAX_LON = 127.0; // 서울 강남/경기 광주 일부 포함
//
//    private static final double LAT_STEP = 0.900;
//    private static final double LON_STEP = 0.900;
//
//    // Grid 셀당 생성할 아이템 수
//    private static final int COWORKER_PER_CELL = 1;
//    private static final int GIFTS_PER_CELL = 1; // 선물상자 수
//    private static final double MAX_JITTER = 0.008; // 셀 내에서 흩뿌릴 최대 오프셋
//
//
//    @PostConstruct
//    public void initialMapGeneration() {
//        log.info("--- [Initializer] 서버 시작-");
//        redisTemplate.execute((RedisConnection connection) -> {
//            connection.serverCommands().flushDb();
//            return null;
//        });
//        generateFullMapData();
//        log.info("--- [Initializer] 초기화 완료 ---");
//    }
//
//    @Scheduled(fixedRate = 10800000, initialDelay = 10800000)
//    public void scheduledMapRegeneration() {
//        log.info("--- [Scheduler] 3시간 주기 맵 재생성 시작 ---");
//        generateFullMapData();
//        log.info("--- [Scheduler] 3시간 주기 맵 재생성 완료 ---");
//    }
//
//    public void generateFullMapData() {
//        List<Map<String, String>> allCoworkerHashData = new ArrayList<>();
//
//        redisTemplate.delete(COWORKER_LOCATIONS_KEY);
//        redisTemplate.delete(RANDOM_GIFT_BOX_KEY);
//        log.info("--- 동료/선물상자 기존 데이터 삭제 완료 ---");
//
//        redisTemplate.executePipelined((RedisConnection connection) -> {
//
//            for (double lat = MIN_LAT; lat < MAX_LAT; lat += LAT_STEP) {
//                for (double lon = MIN_LON; lon < MAX_LON; lon += LON_STEP) {
//
//                    double cellCenterLat = lat + (LAT_STEP / 2);
//                    double cellCenterLon = lon + (LON_STEP / 2);
//
//                    createItemsInCell(cellCenterLat, cellCenterLon, COWORKER_PER_CELL, COWORKER_LOCATIONS_KEY, "COWORKER", allCoworkerHashData);
//                    createItemsInCell(cellCenterLat, cellCenterLon, GIFTS_PER_CELL, RANDOM_GIFT_BOX_KEY, "GIFT", allCoworkerHashData);
//                }
//            }
//            return null;
//        });
//
//        if (!allCoworkerHashData.isEmpty()) {
//            redisTemplate.executePipelined((RedisConnection connection) -> {
//                for (Map<String, String> data : allCoworkerHashData) {
//                    String coworkerId = data.get("id");
//                    String instanceKey = COWORKER_DATA_PREFIX + coworkerId;
//
//                    // Hash 데이터 저장 및 TTL 설정
//                    redisTemplate.opsForHash().putAll(instanceKey, data);
//                    redisTemplate.expire(instanceKey, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
//                }
//                log.info("--- 2차 Pipeline (Hash Data) {}개 저장 완료 ---", allCoworkerHashData.size());
//                return null;
//            });
//        }
//    }
//
//    /**
//     * Helper: 특정 셀 중심에 N개의 아이템을 랜덤하게 생성하여 Redis에 저장합니다.
//     */
//    private void createItemsInCell(
//            double centerLat, double centerLon, int count, String key, String prefix, List<Map<String, String>> allCoworkerHashData
//    ) {
//
//        Map<String, Point> locations = new HashMap<>();
//
//        for (int i = 0; i < count; i++) {
//            String memberName = prefix + "_" + UUID.randomUUID().toString().substring(0, 18);
//
//            // 셀 중심 좌표에서 MAX_JITTER 내에서 랜덤하게 위치를 흩뿌립니다.
//            double randomLat = generateRandomCoordinate(centerLat, MAX_JITTER);
//            double randomLon = generateRandomCoordinate(centerLon, MAX_JITTER);
//
//            locations.put(memberName, new Point(randomLon, randomLat));
//
//            if (key.equals(COWORKER_LOCATIONS_KEY)) {
//                // ⭐ Hash 데이터를 수집합니다. ⭐
//                // (이 메서드는 Redis I/O 없이 Map을 반환하도록 수정되어야 합니다.)
//                Map<String, String> hashData = createAndSaveCoworkerHashData(memberName);
//
//                if (!hashData.isEmpty()) {
//                    allCoworkerHashData.add(hashData);
//                }
//            }
//        }
//
//        if (!locations.isEmpty()) {
//            redisTemplate.opsForGeo().add(key, locations);
//            redisTemplate.expire(key, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
//        }
//    }
//
//    private Map<String, String> createAndSaveCoworkerHashData(final String coworkerId) {
//        Map<String, String> dataToSave = coworkerDataCreator.createRandomCoworkerAttribute();
//        if (dataToSave.isEmpty()) {
//            return Collections.emptyMap();
//        }
//        dataToSave.put("id", coworkerId);
//        return dataToSave;
//    }
//
//    private double generateRandomCoordinate(double center, double maxOffset) {
//        double offset = ThreadLocalRandom.current().nextDouble(-maxOffset, maxOffset);
//        return Math.round((center + offset) * 1000000.0) / 1000000.0;
//    }
//
//}
