package com.game.subwayworkerserver.domain.reward;

public record UserLevelUpReward(
        long coin,
        int ticketCount,
        int cash
) {
}