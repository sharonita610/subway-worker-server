package com.game.subwayworkerserver.service.common;

import com.game.subwayworkerserver.domain.reward.RewardType;
import com.game.subwayworkerserver.domain.reward.RewardSource;
import com.game.subwayworkerserver.domain.reward.CheckInReward;
import com.game.subwayworkerserver.controller.request.SystemMailRequest;
import com.game.subwayworkerserver.controller.request.SystemMailRequest.SystemRewardRequest;
import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static com.game.subwayworkerserver.domain.reward.RewardSource.SUBSCRIPTION;
import static com.game.subwayworkerserver.domain.reward.RewardSource.SYSTEM_REWARD;
import static com.game.subwayworkerserver.domain.reward.RewardType.*;

@Component
@RequiredArgsConstructor
public class RewardProcessor {

    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    private static final String REWARD_CONTENT = "보상이 지급되었어요. 받아가세요!";

    public void processAttendanceReward(final UserEntity user, final CheckInReward reward, final long rewardDay) {
        long userId = user.getId();
        long mailId = createMail(userId, String.format("%d일차 보상 지급", rewardDay), REWARD_CONTENT);

        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (reward.getCoin() > 0) {
            rewardsToSave.add(
                    getBuild(userId, RewardType.COIN, RewardSource.ATTENDANCE, reward.getCoin(), mailId)
            );
        }
        if (reward.getVacationTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, VACATION_TICKET, RewardSource.ATTENDANCE, reward.getVacationTicket(), mailId)
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

    private long createMail(long userId, String title, String content) {
        MailEntity mailEntity = MailEntity.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isRead(false)
                .isClaimed(false)
                .build();

        MailEntity mail = mailRepository.save(mailEntity);
        return mail.getId();
    }

    @Transactional
    public void sendDailySubscriptionReward(long userId, String mailTitle, int dailyTicket) {
        userRepository.findById(userId).ifPresent(UserEntity::hasMailToCheck);
        long mailId = createMail(userId, mailTitle, REWARD_CONTENT);
        RewardEntity build = getBuild(userId, VACATION_TICKET, SUBSCRIPTION, dailyTicket, mailId);
        rewardRepository.save(build);
    }

    public void processSubscriptionReward(final UserEntity user, final SubscriptionItem subscriptionItem) {
        long userId = user.getId();

        long mailId = createMail(user.getId(), "Vip 뱃지 구매", REWARD_CONTENT);
        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (subscriptionItem.getDailyTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, VACATION_TICKET, SUBSCRIPTION, subscriptionItem.getDailyTicket(), mailId)
            );
        }
        if (subscriptionItem.getInstanceTicket() > 0) {
            rewardsToSave.add(
                    getBuild(userId, VACATION_TICKET, SUBSCRIPTION, subscriptionItem.getInstanceTicket(), mailId)
            );
        }
        rewardRepository.saveAll(rewardsToSave);
        user.hasMailToCheck();
    }

    private static final int SYSTEM_MAIL_BATCH_SIZE = 500;

    @Transactional
    public void sendSystemMail(final SystemMailRequest request) {
        int page = 0;
        List<UserEntity> batch;
        do {
            batch = userRepository.findAll(PageRequest.of(page++, SYSTEM_MAIL_BATCH_SIZE)).getContent();
            batch.forEach(userEntity -> {
                long mailId = createMail(userEntity.getId(), request.title(), request.content());
                if (!request.rewards().isEmpty()) {
                    processSystemMail(userEntity, mailId, request.rewards());
                }
            });
        } while (batch.size() == SYSTEM_MAIL_BATCH_SIZE);
    }

    private void processSystemMail(
            final UserEntity user, final long mailId, List<SystemRewardRequest> rewards
    ) {
        Long userId = user.getId();

        List<RewardEntity> rewardsToSave = new ArrayList<>();
        rewards.forEach(reward -> {
            switch (reward.rewardName()) {
                case "GACHA_TICKET" -> rewardsToSave.add(
                        getBuild(userId, GACHA_TICKET, SYSTEM_REWARD, reward.quantity(), mailId)
                );
                case "VACATION_TICKET" -> rewardsToSave.add(
                        getBuild(userId, VACATION_TICKET, SYSTEM_REWARD, reward.quantity(), mailId)
                );
                case "COIN" -> rewardsToSave.add(
                        getBuild(userId, COIN, SYSTEM_REWARD, reward.quantity(), mailId)
                );
            }

        });
        rewardRepository.saveAll(rewardsToSave);
        user.hasMailToCheck();
    }
}
