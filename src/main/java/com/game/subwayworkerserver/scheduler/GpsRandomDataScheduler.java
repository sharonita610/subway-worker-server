package com.game.subwayworkerserver.scheduler;

import com.game.subwayworkerserver.service.CoworkerDataCreator;
import com.game.subwayworkerserver.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Component;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpsRandomDataScheduler {

    private final CoworkerDataCreator coworkerDataCreator;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Key 상수
    private static final String COWORKER_LOCATIONS_KEY = "coworker:locations";
    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final String COWORKER_DATA_PREFIX = "coworker:data:";
    private static final long MAP_DATA_TTL_DAYS = 1;
    private static final long MAP_DATA_TTL_HOURS = 4;
    // 대한민국 지도 범위 (대략적)
    private static final double MIN_LAT = 33.0;
    private static final double MAX_LAT = 38.5;
    private static final double MIN_LON = 124.0;
    private static final double MAX_LON = 131.0;

    // 1km Grid 셀 간격 (약 1km에 해당하는 도(degree) 값)
    // 2km Grid 셀 간격 (약 2km에 해당하는 도(degree) 값)
    private static final double LAT_STEP = 0.018;
    private static final double LON_STEP = 0.022;

    // Grid 셀당 생성할 아이템 수
    private static final int COWORKER_PER_CELL = 10;
    private static final int GIFTS_PER_CELL = 4; // 선물상자 수
    private static final double MAX_JITTER = 0.008; // 셀 내에서 흩뿌릴 최대 오프셋


    // --- 스케줄링 및 초기화 ---

    /**
     * 1. 서버 시작 시 초기 맵 생성 (일반 아이템은 이 때 한 번만 생성됩니다.)
     */
    @PostConstruct
    public void initialMapGeneration() {
        log.info("--- [Initializer] 서버 시작, 초기 맵 생성 시작 ---");
        // Item 생성 포함하여 맵 초기화
        generateFullMapData(false);
        log.info("--- [Initializer] 초기 맵 생성 완료 ---");
    }

    /**
     * 2. 30분마다 맵의 동료와 선물 상자 데이터를 재생성합니다.
     * fixedRate = 1800000ms (30분)
     */
    @Scheduled(fixedRate = 10800000)
    public void scheduledMapRegeneration() {
        log.info("--- [Scheduler] 3시간 주기 맵 재생성 시작 ---");
        // Item 생성/삭제를 건너뛰고, 동료와 선물상자만 재생성
        generateFullMapData(false);
        log.info("--- [Scheduler] 3시간 주기 맵 재생성 완료 ---");
    }

    /**
     * 핵심 로직: Redis의 데이터를 삭제하고 새로운 Grid 데이터를 생성합니다.
     *
     * @param isScheduledExecution 스케줄링 실행 여부 (true인 경우 Item 생성 건너뛰기)
     */
    public void generateFullMapData(boolean isScheduledExecution) {

        if (!isScheduledExecution) {
            // 1. 기존 데이터 삭제 (Item_Locations_Key는 제외)
            redisTemplate.delete(COWORKER_LOCATIONS_KEY);
            redisTemplate.delete(RANDOM_GIFT_BOX_KEY);
            redisTemplate.delete(COWORKER_DATA_PREFIX);
            log.info("--- 동료/선물상자 기존 데이터 삭제 완료 ---");
        }

        int totalCells = 0;
        int totalCoworkersAndGifts = 0;
        redisTemplate.executePipelined((RedisConnection connection) -> {

            // 2. 대한민국 Grid 순회 및 데이터 생성
            for (double lat = MIN_LAT; lat < MAX_LAT; lat += LAT_STEP) {
                for (double lon = MIN_LON; lon < MAX_LON; lon += LON_STEP) {

                    double cellCenterLat = lat + (LAT_STEP / 2);
                    double cellCenterLon = lon + (LON_STEP / 2);

                    // 동료 생성 (1개) - createItemsInCell 내부의 redisTemplate 호출이 Pipeline에 기록됨
                    createItemsInCell(cellCenterLat, cellCenterLon, COWORKER_PER_CELL, COWORKER_LOCATIONS_KEY, "COWORKER");

                    // 선물 상자 생성 (2개)
                    createItemsInCell(cellCenterLat, cellCenterLon, GIFTS_PER_CELL, RANDOM_GIFT_BOX_KEY, "GIFT");
                }
            }

            // ⭐ 루프가 완전히 끝난 후 Pipeline 작업이 Redis로 전송됩니다. ⭐
            return null;
        });
    }

    /**
     * Helper: 특정 셀 중심에 N개의 아이템을 랜덤하게 생성하여 Redis에 저장합니다.
     */
    private void createItemsInCell(double centerLat, double centerLon, int count, String key, String prefix) {

        Map<String, Point> locations = new HashMap<>();

        for (int i = 0; i < count; i++) {
            String memberName = prefix + "_" + UUID.randomUUID().toString().substring(0, 18);

            // 셀 중심 좌표에서 MAX_JITTER 내에서 랜덤하게 위치를 흩뿌립니다.
            double randomLat = generateRandomCoordinate(centerLat, MAX_JITTER);
            double randomLon = generateRandomCoordinate(centerLon, MAX_JITTER);

            locations.put(memberName, new Point(randomLon, randomLat));

            if (key.equals(COWORKER_LOCATIONS_KEY)) {
                createAndSaveCoworkerHashData(memberName);
            }
        }

        if (!locations.isEmpty()) {
            redisTemplate.opsForGeo().add(key, locations);
            redisTemplate.expire(key, MAP_DATA_TTL_HOURS, TimeUnit.HOURS);
        }
    }

    private void createAndSaveCoworkerHashData(final String coworkerId) {
        Map<String, String> dataToSave = coworkerDataCreator.createRandomCoworkerAttribute();
        if (dataToSave.isEmpty()) {
            return;
        }
        String instanceKey = COWORKER_DATA_PREFIX + coworkerId;
        redisTemplate.opsForHash().putAll(instanceKey, dataToSave);
        redisTemplate.expire(instanceKey, MAP_DATA_TTL_DAYS, TimeUnit.DAYS);
    }

    private double generateRandomCoordinate(double center, double maxOffset) {
        double offset = ThreadLocalRandom.current().nextDouble(-maxOffset, maxOffset);
        return Math.round((center + offset) * 1000000.0) / 1000000.0;
    }

}
