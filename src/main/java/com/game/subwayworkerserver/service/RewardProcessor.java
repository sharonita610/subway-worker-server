package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RewardProcessor {

    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    private static final String REWARD_CONTENT = "보상이 지급되었어요. 받아가세요!";

    public void process(final UserEntity user, final CheckInReward reward, final long rewardDay) {
        long userId = user.getId();
        long mailId = createMail(userId, String.format("%d일차 보상 지급", rewardDay));

        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (reward.getCoin() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.COIN, RewardSource.ATTENDANCE, reward.getCoin(), mailId)
            );
        }
        if (reward.getVacationTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.VACATION_TICKET, RewardSource.ATTENDANCE, reward.getVacationTicket(), mailId)
            );
        }
        if (reward.getGachaTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.GACHA_TICKET, RewardSource.ATTENDANCE, reward.getGachaTicket(), mailId)
            );
        }
        if (reward.getMaterialFriend() != null) {
            rewardsToSave.add(
                    getBuildWithExtraInfo(userId, RewardType.ENERZ, RewardSource.ATTENDANCE, mailId, String.valueOf(reward.getMaterialFriend().getLevel()))
            );
        }
        if (reward.getNoAds() != null) {
            rewardsToSave.add(
                    getBuildWithExtraInfo(userId, RewardType.NO_ADS, RewardSource.ATTENDANCE, mailId, String.valueOf(reward.getNoAds().getDays()))
            );
        }

        rewardRepository.saveAll(rewardsToSave);
    }

    private static RewardEntity getBuildWithExtraInfo(long userId, RewardType type, RewardSource source, long mailId, String extraInfo) {
        return RewardEntity.builder()
                .userId(userId)
                .type(type)
                .source(source)
                .quantity(1)
                .isClaimed(false)
                .mailId(mailId)
                .extraInfo(extraInfo)
                .build();
    }

    private static RewardEntity getBuild(long userId, RewardType type, RewardSource source, long quantity, long mailId) {
        return RewardEntity.builder()
                .userId(userId)
                .type(type)
                .source(source)
                .quantity(quantity)
                .isClaimed(false)
                .mailId(mailId)
                .build();
    }

    private long createMail(long userId, String title) {
        MailEntity mailEntity = MailEntity.builder()
                .userId(userId)
                .title(title)
                .content(REWARD_CONTENT)
                .isRead(false)
                .isClaimed(false)
                .build();

        MailEntity mail = mailRepository.save(mailEntity);
        return mail.getId();
    }

    @Transactional
    public void sendDailySubscriptionReward(long userId, String mailTitle, int dailyTicket) {
        userRepository.findById(userId).ifPresent(UserEntity::hasMailToCheck);
        long mailId = createMail(userId, mailTitle);
        RewardEntity build = getBuild(userId, RewardType.VACATION_TICKET, RewardSource.SUBSCRIPTION, dailyTicket, mailId);
        rewardRepository.save(build);
    }
}
