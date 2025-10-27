package com.game.subwayworkerserver.domain.reward;

public record BaseReward(
        int exp,
        int coin
) {

    public static BaseReward getCaptureReward() {
        return new BaseReward(30, 3000);
    }

    public static BaseReward getFailedCaptureReward() {
        return new BaseReward(0, 0);
    }

    public static BaseReward getTrapReward() {
        return new BaseReward(50, 5000);
    }
}
