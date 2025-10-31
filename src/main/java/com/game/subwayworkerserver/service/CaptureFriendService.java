package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CaptureAvailableFriendRequest;
import com.game.subwayworkerserver.controller.request.CaptureFriendRequest;
import com.game.subwayworkerserver.controller.response.CaptureAvailableFriendResponse;
import com.game.subwayworkerserver.controller.response.CaptureResponse;
import com.game.subwayworkerserver.domain.baseinfo.FriendAttribute;
import com.game.subwayworkerserver.domain.baseinfo.FriendLevel;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CaptureRecordRepository;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CaptureFriendService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserRepository userRepository;
    private final CaptureRecordRepository captureRecordRepository;
    private final UserItemMapRepository  userItemMapRepository;
    private final FriendRepository friendRepository;

    private static final String FRIEND_LOCATIONS_KEY = "friend:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    private static final String FRIEND_DATA_PREFIX = "friend:data:";

    @Transactional
    public CaptureResponse tryToCapture(final String uuid, final CaptureFriendRequest request) {

        final String acquiredKey = USER_ACQUIRED_PREFIX + uuid + ":" + FRIEND_LOCATIONS_KEY;
        final String friendId = request.friendId();

        // Redis Set에 해당 friendId가 이미 존재하는지 확인
        Boolean isAlreadyHidden = redisTemplate.opsForSet().isMember(acquiredKey, friendId);
        if (isAlreadyHidden != null && isAlreadyHidden) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 획득했거나 도망쳐서 포획할 수 없는 동료입니다.");
        }

        String instanceKey = FRIEND_DATA_PREFIX + request.friendId();
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(instanceKey);

        if (hashData.isEmpty()) {
            throw new BaseException(ErrorCode.NOT_FOUND, "포획하고자 하는 동료 조회 에러 발생.");
        }

        // user 정보 조회
        long userId = userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        ).getId();

        // user item map 에서 도구 조회
        ItemName itemName = ItemName.fromConstantName(request.captureItemName());
        UserItemMapEntity userItemMapEntity = userItemMapRepository.findByUserIdAndItemName(userId, itemName)
                .orElseThrow(
                        () -> new BaseException(ErrorCode.NOT_FOUND, "사용 가능한 포획도구가 더이상 없습니다.")
                );

        // 도구 사용
        userItemMapEntity.decreaseQuantity();

        //  포획 기록
        captureRecordRepository.findByUserId(userId).ifPresentOrElse(
                CaptureRecordEntity::increaseTryCount,
                () -> {
                    CaptureRecordEntity captureRecordEntity = CaptureRecordEntity.of(userId);
                    captureRecordRepository.save(captureRecordEntity);
                }
        );

        CaptureRecordEntity captureRecord = captureRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR));

        int captureAttemptCount = Integer.parseInt((String)hashData.get("captureAttemptCount"));

        boolean succeed = ItemName.canCaptureSucceed(request.captureItemName());
        if (succeed) {
            String friendName = (String) hashData.get("friendName");
            int friendLevel = Integer.parseInt((String) hashData.get("friendLevel"));
            String position = (String) hashData.get("position");

            FriendEntity friendEntity = FriendEntity.of(userId, friendName, friendLevel);
            friendRepository.save(friendEntity);

            hideFriendFromUser(request.friendId(), acquiredKey);
            captureRecord.increaseSuccessCount();

            return CaptureResponse.of(captureAttemptCount, friendEntity, position);
        }

        // 3. 포획 실패 (Failure)
        else {
            // 3-1. 시도 횟수 증가 및 Redis에 업데이트 (실패해도 시도 횟수는 증가해야 함)
            int newAttemptCount = captureAttemptCount + 1;
            redisTemplate.opsForHash().put(instanceKey, "captureAttemptCount", String.valueOf(newAttemptCount));

            captureRecord.increaseFailCount();

            // 3-2. 30% 확률로 도망갈지 시뮬레이션
            final int ESCAPE_PROBABILITY = 30; // 30% 도망 확률
            int randomRoll = ThreadLocalRandom.current().nextInt(100); // 0부터 99까지 난수

            if (randomRoll < ESCAPE_PROBABILITY) {
                // 3-2-1. 도망 성공 (Escape - 30%)
                // 도망갔으므로, 해당 동료 인스턴스를 Redis에서 삭제하는 로직이 필요합니다.
                hideFriendFromUser(request.friendId(), acquiredKey);
                return CaptureResponse.of(false, true);

            } else {
                // 3-2-2. 도망 실패 (Failure, Remain - 70%)
                return CaptureResponse.of(false, false);
            }
        }
    }

    private void hideFriendFromUser(final String friendId,  final String acquiredKey) {
        redisTemplate.opsForSet().add(acquiredKey, friendId);
    }
}
