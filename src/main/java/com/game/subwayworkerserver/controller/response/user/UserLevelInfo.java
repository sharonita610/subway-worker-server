package com.game.subwayworkerserver.controller.response.user;

import com.game.subwayworkerserver.domain.level.UserLevelConfig;
import com.game.subwayworkerserver.entity.UserEntity;

public record UserLevelInfo(
        int currentLevel,
        long currentExp,
        long totalCumulativeExp,
        long requiredExpToNextLevel,
        int stageLevel
) {
    public static UserLevelInfo of(UserEntity user) {
        return new UserLevelInfo(
                user.getLevel(),
                user.getExp(),
                user.getTotalExp(),
                UserLevelConfig.getRequiredExpForLevel(user.getLevel()),
                user.getStage()
        );
    }
}
