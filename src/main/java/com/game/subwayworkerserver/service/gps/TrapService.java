package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.common.UserLevelProcessor;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.item.ItemCategory;
import com.game.subwayworkerserver.dto.request.gps.GpsAddTrapRequest;
import com.game.subwayworkerserver.dto.response.coworker.CoworkerListResponse;
import com.game.subwayworkerserver.dto.response.gps.UserTrapListResponse;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserTrapMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrapService {

    private final UserFinder userFinder;
    private final UserItemMapRepository userItemMapRepository;
    private final UserTrapMapRepository userTrapMapRepository;
    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final UserLevelProcessor userLevelProcessor;
    private final CaptureCoworkerProcessor captureCoworkerProcessor;

    private final RedisTemplate<String, String> redisTemplate;

    private static final long TRAP_VALID_HOURS = 12;
    private static final double TRAP_CAPTURE_RADIUS_METER = 25.0;
    private static final long TRAPPED_DATA_TTL_DAYS = 7;

    /**
     * 덫 설치 API 요청을 처리하고, 아이템을 소모하여 트랩 정보를 DB에 저장합니다.
     *
     * @param uuid    현재 유저의 UUID
     * @param request 설치할 트랩 정보 (아이템 이름, 위도, 경도)
     */
    @Transactional
    public void setTrap(String uuid, GpsAddTrapRequest request) {

        UserEntity user = userFinder.getByUuid(uuid);
        long userId = user.getId();

        ItemName trapItem;
        try {
            trapItem = ItemName.fromConstantName(request.trapName());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효하지 않은 트랩 아이템 이름입니다.");
        }

        if (trapItem.getCategory() != ItemCategory.TRAP) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "선택된 아이템은 트랩 설치 도구가 아닙니다.");
        }
        UserItemMapEntity itemMap = userItemMapRepository.findByUserIdAndItemName(userId, trapItem)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "인벤토리에 해당 트랩 아이템이 없습니다."));

        // 수량 확인 및 감소 (decreaseQuantity는 0 미만 방지 로직을 포함)
        try {
            itemMap.decreaseQuantity();
        } catch (IllegalStateException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "트랩 아이템 수량이 부족합니다.");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TRAP_VALID_HOURS);
        UserTrapMapEntity setTrap = UserTrapMapEntity.of(
                userId,
                trapItem,
                request.latitude(),
                request.longitude(),
                expiresAt
        );
        userTrapMapRepository.save(setTrap);

        log.info("User {} set trap '{}' at ({}, {}). Expires at: {}",
                userId, trapItem.name(), request.latitude(), request.longitude(), expiresAt);
    }

    public UserTrapListResponse findTrapByUser(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<UserTrapMapEntity> userTrapMapList =
                userTrapMapRepository.findByUserIdAndExpiresAtAfterAndIsTrappedFalse(user.getId(), LocalDateTime.now());
        return UserTrapListResponse.of(userTrapMapList);
    }

    @Transactional
    public CoworkerListResponse getTrapSuccessReward(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<UserTrapMapEntity> userTrapMapList = userTrapMapRepository.findByUserIdAndIsTrappedTrue(user.getId());
        List<CoworkerEntity> coworkerEntityList = saveUserTrapIfSuccess(userTrapMapList);
        user.updateTrapDone();

        if (coworkerEntityList.isEmpty()) {
            return CoworkerListResponse.empty();
        }
        int size = coworkerEntityList.size();
        BaseReward trapReward = BaseReward.getTrapReward();

        int totalCoin = trapReward.coin() * size;
        int totalExp = trapReward.exp() * size;
        user.increaseCoin(totalCoin);

        userLevelProcessor.processExpGainAndLevelUp(user, totalExp);

        return CoworkerListResponse.of(totalCoin, totalExp, coworkerEntityList);
    }

    /**
     * 만료된 트랩을 정리한 뒤, 유효한 트랩의 포획 반경 안에 들어온 동료를 포획 처리한다.
     * 포획된 동료의 Redis 데이터는 보상 수령 시점까지 별도 키에 백업해 둔다.
     *
     * @return 이번 실행에서 포획에 성공한 트랩 수
     */
    @Transactional
    public int checkTrapsForCapture() {
        long deletedCount = cleanUpExpiredTraps();
        log.info("[Trap] 만료된 트랩 {}건 삭제", deletedCount);

        List<UserTrapMapEntity> activeTraps =
                userTrapMapRepository.findByExpiresAtAfterAndIsTrappedFalse(LocalDateTime.now());
        if (activeTraps.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (UserTrapMapEntity trap : activeTraps) {
            String coworkerId = findCoworkerNearby(trap);
            if (coworkerId == null) {
                continue;
            }
            backupTrappedCoworker(coworkerId);
            trap.markSuccess(coworkerId);
            userRepository.findById(trap.getUserId()).ifPresent(user -> {
                user.updateTrapSuccess();
                String acquiredKey = GpsRedisKeys.USER_ACQUIRED_PREFIX + user.getUuid()
                        + ":" + GpsRedisKeys.COWORKER_LOCATIONS_KEY;
                redisTemplate.opsForSet().add(acquiredKey, coworkerId);
            });
            successCount++;
        }
        return successCount;
    }

    /** 트랩 위치 기준 포획 반경 안에서 가장 가까운 동료 ID를 돌려준다. 없으면 null. */
    private String findCoworkerNearby(UserTrapMapEntity trap) {
        Circle searchArea = new Circle(
                new Point(trap.getLongitude(), trap.getLatitude()),
                new Distance(TRAP_CAPTURE_RADIUS_METER, RedisGeoCommands.DistanceUnit.METERS)
        );
        RedisGeoCommands.GeoRadiusCommandArgs args =
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().limit(1);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(GpsRedisKeys.COWORKER_LOCATIONS_KEY, searchArea, args);

        if (results == null || results.getContent().isEmpty()) {
            return null;
        }
        return results.getContent().get(0).getContent().getName();
    }

    /** 동료 속성 해시를 trapped 키로 복사하고 TTL을 건다. 원본이 없으면 경고만 남긴다. */
    private void backupTrappedCoworker(String coworkerId) {
        String sourceKey = GpsRedisKeys.COWORKER_DATA_PREFIX + coworkerId;
        String backupKey = GpsRedisKeys.TRAPPED_COWORKER_DATA_PREFIX + coworkerId;

        Map<Object, Object> attributes = redisTemplate.opsForHash().entries(sourceKey);
        if (attributes.isEmpty()) {
            log.warn("[Trap] 동료 {} 데이터가 Redis에 없어 백업하지 못했습니다. key={}", coworkerId, sourceKey);
            return;
        }
        redisTemplate.opsForHash().putAll(backupKey, attributes);
        redisTemplate.expire(backupKey, TRAPPED_DATA_TTL_DAYS, TimeUnit.DAYS);
    }

    private long cleanUpExpiredTraps() {
        List<UserTrapMapEntity> expiredTraps =
                userTrapMapRepository.findByExpiresAtBeforeAndIsTrappedFalse(LocalDateTime.now());
        if (expiredTraps.isEmpty()) {
            return 0;
        }
        userTrapMapRepository.deleteAll(expiredTraps);
        return expiredTraps.size();
    }

    private List<CoworkerEntity> saveUserTrapIfSuccess(final List<UserTrapMapEntity> userTrapMapList) {
        return userTrapMapList.stream().map(trapMap -> {
                    ItemName trapItemName = trapMap.getTrapItemName();

                    if (!captureCoworkerProcessor.calculateTrapSucceed(trapItemName)) {
                        trapMap.setIsNotSuccessful();
                        trapMap.setExpiresAt();
                        return null;
                    }

                    String capturedCoworkerId = trapMap.getCapturedCoworkerId();
                    String trappedKey = GpsRedisKeys.TRAPPED_COWORKER_DATA_PREFIX + capturedCoworkerId;
                    Map<Object, Object> hashData = redisTemplate.opsForHash().entries(trappedKey);

                    if (hashData.isEmpty()) {
                        log.info("Null info for instance {}.", trappedKey);
                        trapMap.setIsNotSuccessful();
                        trapMap.setExpiresAt();
                        return null;
                    }

                    String coworkerName = (String) hashData.get("coworkerName");
                    String levelString = (String) hashData.get("coworkerLevel");

                    if (coworkerName != null && levelString != null) {
                        try {
                            int coworkerLevel = Integer.parseInt(levelString);

                            CoworkerEntity coworkerEntity = CoworkerEntity.of(trapMap.getUserId(), coworkerName, coworkerLevel);
                            CoworkerEntity savedCoworker = coworkerRepository.save(coworkerEntity);

                            trapMap.setIsSuccessful();
                            trapMap.setExpiresAt();
                            redisTemplate.delete(trappedKey);

                            return savedCoworker;
                        } catch (NumberFormatException e) {
                            log.error("Invalid coworkerLevel format: {}", levelString);
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
