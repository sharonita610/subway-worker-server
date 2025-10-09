package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.UserEntity;

public record UserLevelInfo(
        int currentLevel,
        long currentExp,
        long totalCumulativeExp
) {
    public static UserLevelInfo of(UserEntity user) {
        return new UserLevelInfo(user.getLevel(), user.getExp(), user.getTotalExp());
    }
}
