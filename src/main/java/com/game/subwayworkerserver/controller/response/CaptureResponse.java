package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.CoworkerEntity;

public record CaptureResponse(
        boolean success,
        BaseReward reward,
        int extraExpReward,
        CapturedCoworkerInfo capturedCoworkerInfo,
        boolean run
) {

    public static CaptureResponse of(final int captureAttemptCount, final CoworkerEntity coworkerEntity, String position) {
        return new CaptureResponse(
            true,
            BaseReward.getCaptureReward(),
            captureAttemptCount == 0 ? 50 : 0,
            CapturedCoworkerInfo.of(coworkerEntity, position),
                false
        );
    }

    public static CaptureResponse of(boolean success, boolean run) {
        return new CaptureResponse(success, BaseReward.getFailedCaptureReward(), 0, null, run);
    }
}
