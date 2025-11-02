package com.game.subwayworkerserver.scheduler;

import com.game.subwayworkerserver.entity.UserTrapMapEntity;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserTrapMapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrapCheckerScheduler {

    private final UserTrapMapRepository userSetTrapRepository;
    private final UserRepository userRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String FRIEND_LOCATIONS_KEY = "friend:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    private static final double TRAP_CAPTURE_RADIUS_METER = 25.0;

    private static final String FRIEND_DATA_PREFIX = "friend:data:";
    private static final String TRAPPED_FRIEND_DATA_PREFIX = "trapped:friend:data:";
    private static final long TRAPPED_DATA_TTL_DAYS = 14;

    /**
     * 💡 3. 트랩-동료 겹침 확인 스케줄러 (30분마다 실행)
     * // 5분 = 300,000ms
     */
    @Async
    @Scheduled(cron = "17 0 */2 * * *")
    @Transactional
    public void checkTrapsForCapture() {
        log.info("--- [Scheduler] 트랩-동료 겹침 확인 시작 ---");

        // 1. 만료된 트랩 정리 (Clean-up) 🧹
        long deletedCount = cleanUpExpiredTraps();
        log.info("-- - 만료되어 삭제된 트랩 수: {}", deletedCount);

        // 1. 현재 유효하고 포획에 성공하지 않은 모든 트랩 조회
        List<UserTrapMapEntity> activeTraps = userSetTrapRepository
                .findByExpiresAtAfterAndIsTrappedFalse(LocalDateTime.now());

        if (activeTraps.isEmpty()) {
            log.info("유효한 트랩이 없습니다. 스킵합니다.");
            return;
        }

        int successCount = 0;

        // 2. 각 트랩에 대해 근처 동료 확인
        for (UserTrapMapEntity trap : activeTraps) {

            // 2-1. 트랩 위치 기반으로 포획 반경 내 동료 검색
            Circle searchArea = new Circle(
                    new Point(trap.getLongitude(), trap.getLatitude()),
                    new Distance(TRAP_CAPTURE_RADIUS_METER, RedisGeoCommands.DistanceUnit.METERS)
            );

            // 반경 내 동료 ID 1개만 검색
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().limit(1);

            GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                    .radius(FRIEND_LOCATIONS_KEY, searchArea, args);

            if (results != null && !results.getContent().isEmpty()) {
                // 2-2. 겹치는 동료 발견 (트랩 성공!)
                RedisGeoCommands.GeoLocation<String> friendLocation = results.getContent().get(0).getContent();
                String friendId = friendLocation.getName();

                String friendDataKey = FRIEND_DATA_PREFIX + friendId;
                String trappedFriendDataKey = TRAPPED_FRIEND_DATA_PREFIX + friendId;

                Map<Object, Object> friendAttributes = redisTemplate.opsForHash().entries(friendDataKey);
                if (!friendAttributes.isEmpty()) {
                    redisTemplate.opsForHash().putAll(trappedFriendDataKey, friendAttributes);
                    // 2. 백업 데이터의 유효 기간(TTL)을 설정합니다.
                    redisTemplate.expire(trappedFriendDataKey, TRAPPED_DATA_TTL_DAYS, TimeUnit.DAYS);
                    log.info("ID {} 동료 속성 백업 완료. 키: {}", friendId, trappedFriendDataKey);
                } else {
                    log.warn("ID {} 동료 상세 데이터({})가 Redis에 존재하지 않아 백업에 실패했습니다.", friendId, friendDataKey);
                }
                // 2-3. 트랩 및 유저 정보 업데이트
                // a) UserSetTrapEntity에 성공 기록
                trap.markSuccess(friendId);

                // b) UserEntity에 성공 플래그 설정 (로그인 시 알림용)
                userRepository.findById(trap.getUserId()).ifPresent(user -> {
                    user.updateTrapSuccess();
                    userRepository.save(user);
                });

                userRepository.findById(trap.getUserId()).ifPresent(user -> {
                    final String acquiredKey = USER_ACQUIRED_PREFIX + user.getUuid() + ":" + FRIEND_LOCATIONS_KEY;
                    redisTemplate.opsForSet().add(acquiredKey, friendId);
                });

                successCount++;
            }
        }

        log.info("--- [Scheduler] 트랩-동료 겹침 확인 완료. 성공 횟수: {} ---", successCount);
    }

    @Transactional
    public long cleanUpExpiredTraps() {
        // 현재 시간보다 만료 시간이 이전인 (isTrapped=false인) 트랩 목록을 조회합니다.
        List<UserTrapMapEntity> expiredTraps = userSetTrapRepository
                .findByExpiresAtBeforeAndIsTrappedFalse(LocalDateTime.now()); // ⚠️ findByExpiresAtBefore... 메서드가 필요합니다.

        if (expiredTraps.isEmpty()) {
            return 0;
        }

        long count = expiredTraps.size();
        userSetTrapRepository.deleteAll(expiredTraps);
        return count;
    }
}