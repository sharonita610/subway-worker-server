package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.FriendOnSale;

public record GlobalFriendInfo(
        int price,
        long sellerId,
        long marketId,
        FriendBaseInfo friendBaseInfo
) {
    public static GlobalFriendInfo of(final FriendOnSale friendOnSale) {
        return new GlobalFriendInfo(
                friendOnSale.price(),
                friendOnSale.sellerId(),
                friendOnSale.marketId(),
                FriendBaseInfo.from(friendOnSale)
        );
    }
}
