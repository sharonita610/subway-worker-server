package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.FriendOnSale;

import java.util.List;

public record FriendsOnSaleResponse(
        List<FriendOnSale> friendOnSaleList
) {
}
