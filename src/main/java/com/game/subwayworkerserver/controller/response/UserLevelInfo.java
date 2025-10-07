package com.game.subwayworkerserver.controller.response;

public record UserLevelInfo(
        int currentLevel,
        long currentExp,
        long totalCumulativeExp
) {
}
