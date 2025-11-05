package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CaptureCoworkerRequest;
import com.game.subwayworkerserver.controller.response.CaptureResponse;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CaptureRecordRepository;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CaptureCoworkerService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserRepository userRepository;
    private final CaptureRecordRepository captureRecordRepository;
    private final UserItemMapRepository  userItemMapRepository;
    private final CoworkerRepository coworkerRepository;

    private static final String COWORKER_LOCATIONS_KEY = "coworker:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    private static final String COWORKER_DATA_PREFIX = "coworker:data:";

    @Transactional
    public CaptureResponse tryToCapture(final String uuid, final CaptureCoworkerRequest request) {

        final String acquiredKey = USER_ACQUIRED_PREFIX + uuid + ":" + COWORKER_LOCATIONS_KEY;
        final String coworkerId = request.coworkerId();

        // Redis Set에 해당 coworkerId가 이미 존재하는지 확인
        Boolean isAlreadyHidden = redisTemplate.opsForSet().isMember(acquiredKey, coworkerId);
        if (isAlreadyHidden != null && isAlreadyHidden) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 획득했거나 도망쳐서 포획할 수 없는 동료입니다.");
        }

        String instanceKey = COWORKER_DATA_PREFIX + request.coworkerId();
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
            String coworkerName = (String) hashData.get("coworkerName");
            int coworkerLevel = Integer.parseInt((String) hashData.get("coworkerLevel"));
            String position = (String) hashData.get("position");

            CoworkerEntity coworkerEntity = CoworkerEntity.of(userId, coworkerName, coworkerLevel);
            coworkerRepository.save(coworkerEntity);

            hideCoworkerFromUser(request.coworkerId(), acquiredKey);
            captureRecord.increaseSuccessCount();

            return CaptureResponse.of(captureAttemptCount, coworkerEntity, position);
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
                hideCoworkerFromUser(request.coworkerId(), acquiredKey);
                return CaptureResponse.of(false, true);

            } else {
                // 3-2-2. 도망 실패 (Failure, Remain - 70%)
                return CaptureResponse.of(false, false);
            }
        }
    }

    private void hideCoworkerFromUser(final String coworkerId, final String acquiredKey) {
        redisTemplate.opsForSet().add(acquiredKey, coworkerId);
    }
}
