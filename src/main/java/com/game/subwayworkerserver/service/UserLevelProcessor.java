package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.level.UserLevelConfig;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.domain.reward.UserLevelUpReward;
import com.game.subwayworkerserver.entity.MailEntity;
import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.entity.RewardType;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserLevelProcessor {

    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;

    private static final int MAX_LEVEL = 40;

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
                grantLevelUpReward(user, config.getBaseReward(), currentLevel); // 증가된 레벨을 전달

            } else {
                break;
            }
        }

        user.updateLevelAndExp(currentLevel, currentExp);
        user.hasReward();
    }

    private void grantLevelUpReward(UserEntity user, UserLevelUpReward reward, int leveledUpTo) {

        String title = String.format("🎉 레벨업 보상: Lv.%d 달성", leveledUpTo);
        String content = String.format("%d 레벨 달성을 축하드립니다! 보상을 우편함에서 수령해주세요.", leveledUpTo);

        MailEntity mail = MailEntity.builder()
                .userId(user.getId())
                .title(title)
                .content(content)
                .build();

        mail = mailRepository.save(mail);
        final Long mailId = mail.getId();

        List<RewardEntity> rewardsToSave = new ArrayList<>();

        if (reward.coin() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .mailId(mailId)
                    .type(RewardType.COIN)
                    .quantity(reward.coin())
                    .build());
        }

        if (reward.vacationTicket() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .mailId(mailId)
                    .type(RewardType.VACATION_TICKET)
                    .quantity(reward.vacationTicket())
                    .build());
        }

        if (reward.gachaTicket() > 0) {
            rewardsToSave.add(RewardEntity.builder()
                    .mailId(mailId)
                    .type(RewardType.GACHA_TICKET)
                    .quantity(reward.vacationTicket())
                    .build());
        }

        rewardRepository.saveAll(rewardsToSave);
    }
}