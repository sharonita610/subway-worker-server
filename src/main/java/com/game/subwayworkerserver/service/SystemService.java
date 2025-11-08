package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.CheckInListResponse;
import com.game.subwayworkerserver.controller.response.LevelUpRewardResponse;
import com.game.subwayworkerserver.controller.response.UserMailBoxResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.user.MailReward;
import com.game.subwayworkerserver.domain.user.UserCheckInReward;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;
    private final CoworkerRepository coworkerRepository;
    private final AttendanceRewardProcessor rewardProcessor;

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
        mail.claimDone();
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
        UserEntity user = getUserByUuid(uuid);
        int claimedRewardDayUpTo = user.getClaimedRewardDayUpTo();
        List<CheckInReward> allRewards = CheckInReward.getAllRewards();
        List<UserCheckInReward> userCheckInRewards = UserCheckInReward.toDomain(claimedRewardDayUpTo, allRewards);
        return CheckInListResponse.of(userCheckInRewards);
    }

    @Transactional
    public void claimCheckInReward(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        int consecutiveAttendanceDays = user.getConsecutiveAttendanceDays();
        int claimedRewardDayUpTo = user.getClaimedRewardDayUpTo();

        validateClaimDates(consecutiveAttendanceDays, claimedRewardDayUpTo);

        int currentDayToProcess = claimedRewardDayUpTo + 1;
        int finalDayClaimed = claimedRewardDayUpTo;

        while (currentDayToProcess <= consecutiveAttendanceDays) {

            CheckInReward reward = CheckInReward.getRewardByDay(currentDayToProcess);
            rewardProcessor.process(user, reward, currentDayToProcess);

            finalDayClaimed = currentDayToProcess;
            currentDayToProcess++;
        }

        user.updateClaimDailyRewardDone(finalDayClaimed);
    }

    private static void validateClaimDates(int consecutiveAttendanceDays, int claimedRewardDayUpTo) {
        if (consecutiveAttendanceDays == 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "현재 수령 가능한 출석 보상이 없습니다.");
        }

        if (claimedRewardDayUpTo >= consecutiveAttendanceDays) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "현재 출석 일차까지 모든 보상을 이미 수령했습니다.");
        }
    }
}
