package com.game.subwayworkerserver.domain.reward;

public record UserLevelReward(
        long coin,
        int ticketCount,
        int cash
) {
}