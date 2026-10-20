package com.game.subwayworkerserver.dto.response.coworker;

import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.CoworkerEntity;

public record CaptureResponse(
        boolean success,
        BaseReward reward,
        int extraExpReward,
        CapturedCoworkerInfo capturedCoworkerInfo,
        boolean run
) {

    public static CaptureResponse of(
         final BaseReward baseReward,   final int extraExpReward, final CoworkerEntity coworkerEntity, String position
    ) {
        return new CaptureResponse(
                true,
                baseReward,
                extraExpReward,
                CapturedCoworkerInfo.of(coworkerEntity, position),
                false
        );
    }

    public static CaptureResponse of(boolean success, boolean run) {
        return new CaptureResponse(success, BaseReward.getFailedCaptureReward(), 0, null, run);
    }
}
