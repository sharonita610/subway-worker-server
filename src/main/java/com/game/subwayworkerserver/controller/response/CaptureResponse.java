package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.reward.BaseReward;

public record CaptureResponse(
        boolean success,
        BaseReward reward,
        int extraExpReward,
        CapturedFriendInfo capturedFriendInfo
) {
}
