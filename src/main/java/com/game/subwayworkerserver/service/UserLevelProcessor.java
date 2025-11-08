package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.level.UserLevelConfig;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.domain.reward.UserLevelUpReward;
import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.entity.RewardSource;
import com.game.subwayworkerserver.entity.RewardType;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserLevelProcessor {

    private final RewardRepository rewardRepository;

    private static final int MAX_LEVEL = 40;

    @Transactional
    public void processExpGainAndLevelUp(UserEntity user, BaseReward captureReward, int gainedExp) {
        user.grantReward(captureReward.exp(), gainedExp, captureReward.coin());
        checkAndProcessLevelUp(user);
    }

    private void checkAndProcessLevelUp(UserEntity user) {
        int currentLevel = user.getLevel();
        int currentExp = user.getExp();

        while (currentLevel < MAX_LEVEL) {

            UserLevelConfig config;
            try {
                config = UserLevelConfig.getConfigByLevel(currentLevel);
            } catch (IllegalArgumentException e) {
                break;
            }

            long requiredExp = UserLevelConfig.getRequiredExpForLevel(currentLevel + 1);

            if (currentExp >= requiredExp) {
                currentExp -= (int) requiredExp;
                currentLevel++;
                grantLevelUpReward(user.getId(), config.getBaseReward());

            } else {
                break;
            }
        }

        user.updateLevelAndExp(currentLevel, currentExp);
        user.hasReward();
    }

    private void grantLevelUpReward(long userId, UserLevelUpReward reward) {

        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (reward.coin() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .userId(userId)
                    .type(RewardType.COIN)
                    .source(RewardSource.LEVEL_UP)
                    .quantity(reward.coin())
                    .isClaimed(false)
                    .build());
        }

        if (reward.vacationTicket() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .userId(userId)
                    .type(RewardType.VACATION_TICKET)
                    .source(RewardSource.LEVEL_UP)
                    .quantity(reward.vacationTicket())
                    .isClaimed(false)
                    .build());
        }

        if (reward.gachaTicket() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .userId(userId)
                    .type(RewardType.GACHA_TICKET)
                    .source(RewardSource.LEVEL_UP)
                    .quantity(reward.vacationTicket())
                    .isClaimed(false)
                    .build());
        }

        rewardRepository.saveAll(rewardsToSave);
    }
}