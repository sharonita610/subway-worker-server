package com.game.subwayworkerserver.dto.response.subscription;

import com.game.subwayworkerserver.domain.user.UserSubscription;

import java.util.List;

public record SubscribeListResponse(
        List<UserSubscription> subscriptionList
) {
    public static SubscribeListResponse of(List<UserSubscription> domain) {
        return new SubscribeListResponse(domain);
    }
}
