package com.game.subwayworkerserver.domain.reward;

public class RandomBoxReward {

    public record DefaultGift(
            String name,
            String koreanName,
            int quantity
    ) {
    }

    public record ExtraGift(
            String name,
            int quantity
    ) {
    }
}
