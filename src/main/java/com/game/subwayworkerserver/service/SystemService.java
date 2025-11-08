package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.CheckInListResponse;
import com.game.subwayworkerserver.controller.response.LevelUpRewardResponse;
import com.game.subwayworkerserver.controller.response.UserMailBoxResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.user.MailReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;
    private final CoworkerRepository coworkerRepository;
    private final AttendanceChecker attendanceChecker;

    public UserMailBoxResponse getMailBox(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<MailEntity> mailList = getMailListByUserId(user.getId());
        List<Long> mailIdList = getMailIdList(mailList);
        List<RewardEntity> allByMailIdIn = getAllRewardsByMailIds(mailIdList);
        List<MailReward> domainList = MailReward.toDomain(mailList, allByMailIdIn);
        return UserMailBoxResponse.of(domainList);
    }

    private List<RewardEntity> getAllRewardsByMailIds(final List<Long> mailIdList) {
        return rewardRepository.findAllByMailIdInAndIsClaimedFalse(mailIdList);
    }

    private List<MailEntity> getMailListByUserId(final long userId) {
        return mailRepository.findByUserIdAndIsReadFalseAndIsClaimedFalse(userId);
    }

    private static List<Long> getMailIdList(final List<MailEntity> mailList) {
        return mailList.stream()
                .map(MailEntity::getId)
                .toList();
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional
    public LevelUpRewardResponse getLevelUpReward(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<RewardEntity> levelUpReward = rewardRepository.findAllByUserIdAndIsClaimedFalseAndSource(
                user.getId(), RewardSource.LEVEL_UP
        );
        if (levelUpReward.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "현재기준 레벨업 보상이 없습니다.");
        }
        levelUpReward.forEach(reward -> {
            user.giveReward(reward.getType(), reward.getQuantity());
            reward.claimRewardDone();
        });

        user.claimRewardDone();
        return LevelUpRewardResponse.of(levelUpReward);
    }

    @Transactional
    public void claimRewardById(final String uuid, final long mailId) {
        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();

        MailEntity mail = getMailById(mailId);
        List<RewardEntity> rewardList = rewardRepository.findAllByMailIdAndUserIdAndIsClaimedFalse(mail.getId(), userId);
        processRewardList(rewardList, userId, user);

        user.claimMailDone();
    }

    private void processRewardList(List<RewardEntity> rewardList, long userId, UserEntity user) {
        rewardList.forEach(reward -> {
            if(reward.getType().equals(RewardType.ENERZ)) {
                processMaterialFriend(userId);
            }
            user.giveReward(reward.getType(), reward.getQuantity());
            reward.claimRewardDone();
        });
    }

    private void processMaterialFriend(final long userId) {
        CoworkerEntity coworkerEntity = CoworkerEntity.of(userId, CoworkerType.ENERZ, CoworkerLevel.getSpecialCoworkerLevel());
        coworkerRepository.save(coworkerEntity);
    }

    private MailEntity getMailById(final long mailId) {
        return mailRepository.findById(mailId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Mail id not found.")
        );
    }

    @Transactional
    public void claimAllRewards(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<MailEntity> mailList = getMailListByUserId(user.getId());
        List<Long> mailIdList = getMailIdList(mailList);
        List<RewardEntity> rewardList = getAllRewardsByMailIds(mailIdList);
        processRewardList(rewardList, user.getId(), user);
        user.claimMailDone();
    }

    public CheckInListResponse getCheckInRewardList(final String uuid) {

        return null;
    }
    @Transactional
    public void checkAndClaimDailyAttendance(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastAttendanceDate();
        int currentDays = user.getConsecutiveAttendanceDays();

        // 1. ⭐️ 마지막 출석일 확인 및 연속성 검사
        if (lastDate != null) {

            // A) 오늘 이미 출석했는지 확인 (하루 기준 00:00:00)
            if (lastDate.isEqual(today)) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "오늘은 이미 출석 보상을 수령했습니다.");
            }

            // B) 어제가 아닌지 확인 (연속 출석 실패 = 리셋 조건)
            LocalDate yesterday = today.minusDays(1);
            if (lastDate.isBefore(yesterday)) {
                // 1일차부터 다시 시작해야 함
                currentDays = 0;
            }
        }

        // 2. 연속 일수 업데이트
        // (실패했거나 처음 출석 시 currentDays=0, 연속 성공 시 currentDays=1~6)
        currentDays++;

        // 3. 7일차를 초과했는지 확인 (7일 주기가 완료되었으므로 다시 1일차로 시작)
        if (currentDays > 7) {
            currentDays = 1;
        }

        // 4. 보상 지급
        CheckInReward reward = CheckInReward.getRewardByDay(currentDays);
        // rewardService.grantRewardToUser(userId, reward);

        // 5. 유저 상태 업데이트
    }
}
