package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.user.UserCheckInReward;

import java.util.List;

public record CheckInListResponse(
        List<UserCheckInReward> rewardList
) {
    public static CheckInListResponse of(List<UserCheckInReward> userCheckInRewards) {
        return new CheckInListResponse(userCheckInRewards);
    }
}
