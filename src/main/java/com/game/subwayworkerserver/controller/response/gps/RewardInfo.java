package com.game.subwayworkerserver.controller.response.gps;

public record RewardInfo (
        String name,
        int quantity
) {
    public static RewardInfo of(final String name, final int quantity) {
        return new RewardInfo(name, quantity);
    }
}
