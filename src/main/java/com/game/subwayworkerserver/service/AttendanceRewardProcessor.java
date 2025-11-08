package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceRewardProcessor {

    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;

    private static final String REWARD_CONTENT = "보상이 지급되었어요. 받아가세요!";

    public void process(final UserEntity user, final CheckInReward reward, final long rewardDay) {
        long userId = user.getId();
        long mailId = createMail(rewardDay, userId);

        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (reward.getCoin() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.COIN, reward.getCoin(), mailId)
            );
        }
        if (reward.getVacationTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.VACATION_TICKET, reward.getVacationTicket(), mailId)
            );
        }
        if (reward.getGachaTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.GACHA_TICKET, reward.getGachaTicket(), mailId)
            );
        }
        if (reward.getMaterialFriend() != null) {
            rewardsToSave.add(
                    getBuildWithExtraInfo(userId, RewardType.ENERZ, mailId, reward.getMaterialFriend().name())
            );
        }
        if (reward.getNoAds() != null) {
            rewardsToSave.add(
                    getBuildWithExtraInfo(userId, RewardType.NO_ADS, mailId, reward.getNoAds().name())
            );
        }

        rewardRepository.saveAll(rewardsToSave);
    }

    private static RewardEntity getBuildWithExtraInfo(long userId, RewardType type, long mailId, String extraInfo) {
        return RewardEntity.builder()
                .userId(userId)
                .type(type)
                .source(RewardSource.ATTENDANCE)
                .quantity(1)
                .isClaimed(false)
                .mailId(mailId)
                .extraInfo(extraInfo)
                .build();
    }

    private static RewardEntity getBuild(long userId, RewardType type, long quantity, long mailId) {
        return RewardEntity.builder()
                .userId(userId)
                .type(type)
                .source(RewardSource.ATTENDANCE)
                .quantity(quantity)
                .isClaimed(false)
                .mailId(mailId)
                .build();
    }

    private long createMail(long rewardDay, long userId) {
        MailEntity mailEntity = MailEntity.builder()
                .userId(userId)
                .title(String.format("%d일차 보상 지급", rewardDay))
                .content(REWARD_CONTENT)
                .isRead(false)
                .isClaimed(false)
                .build();

        MailEntity mail = mailRepository.save(mailEntity);
        return mail.getId();
    }
}
