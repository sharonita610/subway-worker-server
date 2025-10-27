package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.FriendEntity;

public record CaptureResponse(
        boolean success,
        BaseReward reward,
        int extraExpReward,
        CapturedFriendInfo capturedFriendInfo,
        boolean run
) {

    public static CaptureResponse of(final int captureAttemptCount, final FriendEntity friendEntity, String position) {
        return new CaptureResponse(
            true,
            BaseReward.getCaptureReward(),
            captureAttemptCount == 0 ? 50 : 0,
            CapturedFriendInfo.of(friendEntity, position),
                false
        );
    }

    public static CaptureResponse of(boolean success, boolean run) {
        return new CaptureResponse(success, BaseReward.getFailedCaptureReward(), 0, null, run);
    }
}
