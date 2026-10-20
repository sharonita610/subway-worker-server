package com.game.subwayworkerserver.dto.response.user;

import com.game.subwayworkerserver.dto.response.common.RewardInfo;
import com.game.subwayworkerserver.entity.RewardEntity;

import java.util.List;

public record LevelUpRewardResponse(
        List<RewardInfo> rewardList
) {
    public static LevelUpRewardResponse of(final List<RewardEntity> levelUpReward) {
        return new LevelUpRewardResponse(
                levelUpReward.stream()
                        .map(reward-> RewardInfo.of(reward.getType().name(), (int) reward.getQuantity()))
                .toList()
        ) ;
    }
}
