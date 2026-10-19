package com.game.subwayworkerserver.domain.level;

import lombok.Builder;

@Builder
public record UserLevel(
        int currentLevel,
        long currentExp,
        long totalCumulativeExp
) {

    public long getRequiredExpToNextLevel() {
        if (currentLevel >= UserLevelConfig.LEVEL_RANGE_31_40.getMaxLevel()) {
            return -1; // 만렙 또는 최대 레벨 초과
        }
        return UserLevelConfig.getConfigByLevel(currentLevel).getRequiredExpForLevel(currentLevel);
    }
}