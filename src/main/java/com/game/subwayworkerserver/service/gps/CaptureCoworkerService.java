package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.common.UserLevelProcessor;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.dto.request.gps.CaptureCoworkerRequest;
import com.game.subwayworkerserver.dto.response.coworker.CaptureResponse;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CaptureRecordRepository;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptureCoworkerService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserFinder userFinder;
    private final CaptureRecordRepository captureRecordRepository;
    private final UserItemMapRepository userItemMapRepository;
    private final CoworkerRepository coworkerRepository;
    private final UserLevelProcessor userLevelProcessor;
    private final CaptureCoworkerProcessor captureCoworkerProcessor;

    private static final long ACQUIRED_KEY_TTL_HOURS = 4;

    @Transactional
    public CaptureResponse tryToCapture(final String uuid, final CaptureCoworkerRequest request) {

        final String acquiredKey = GpsRedisKeys.USER_ACQUIRED_PREFIX + uuid + ":" + GpsRedisKeys.COWORKER_LOCATIONS_KEY;
        final String coworkerId = request.coworkerId();

        Boolean isAlreadyHidden = redisTemplate.opsForSet().isMember(acquiredKey, coworkerId);
        if (isAlreadyHidden != null && isAlreadyHidden) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 획득했거나 도망쳐서 포획할 수 없는 동료입니다.");
        }

        String instanceKey = GpsRedisKeys.COWORKER_DATA_PREFIX + request.coworkerId();
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(instanceKey);

        if (hashData.isEmpty()) {
            throw new BaseException(ErrorCode.NOT_FOUND, "포획하고자 하는 동료 조회 에러 발생.");
        }

        UserEntity user = userFinder.getByUuid(uuid);
        long userId = user.getId();

        ItemName itemName = ItemName.fromConstantName(request.captureItemName());
        UserItemMapEntity userItemMapEntity = userItemMapRepository.findByUserIdAndItemName(userId, itemName)
                .orElseThrow(
                        () -> new BaseException(ErrorCode.NOT_FOUND, "사용 가능한 포획도구가 더이상 없습니다.")
                );

        userItemMapEntity.decreaseQuantity();

        CaptureRecordEntity captureRecord = captureRecordRepository.findByUserId(userId)
                .orElseGet(() -> captureRecordRepository.save(CaptureRecordEntity.of(userId)));
        captureRecord.increaseTryCount();

        int captureAttemptCount = Integer.parseInt((String) hashData.get("captureAttemptCount"));
        int coworkerLevel = Integer.parseInt((String) hashData.get("coworkerLevel"));

        boolean succeed = captureCoworkerProcessor.calculateCaptureSucceed(request.captureItemName(), coworkerLevel);
        if (succeed) {
            String coworkerName = (String) hashData.get("coworkerName");
            String position = (String) hashData.get("position");

            CoworkerEntity coworkerEntity = CoworkerEntity.of(userId, coworkerName, coworkerLevel);
            coworkerRepository.save(coworkerEntity);

            hideCoworkerFromUser(request.coworkerId(), acquiredKey);
            captureRecord.increaseSuccessCount();

            BaseReward captureReward = BaseReward.getCaptureReward();
            int extraExp = captureAttemptCount == 0 ? 50 : 0;
            userLevelProcessor.processExpGainAndLevelUp(user, captureReward, extraExp);

            return CaptureResponse.of(captureReward, extraExp, coworkerEntity, position);
        }

        else {
            int newAttemptCount = captureAttemptCount + 1;
            redisTemplate.opsForHash().put(instanceKey, "captureAttemptCount", String.valueOf(newAttemptCount));

            captureRecord.increaseFailCount();

            boolean escaped = captureCoworkerProcessor.calculateEscape(coworkerLevel);
            if (escaped) {
                hideCoworkerFromUser(request.coworkerId(), acquiredKey);
            }
            return CaptureResponse.of(false, escaped);
        }
    }

    private void hideCoworkerFromUser(final String coworkerId, final String acquiredKey) {
        redisTemplate.opsForSet().add(acquiredKey, coworkerId);
        redisTemplate.expire(acquiredKey, ACQUIRED_KEY_TTL_HOURS, TimeUnit.HOURS);
    }
}
