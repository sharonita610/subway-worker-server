package com.game.subwayworkerserver.domain.market;

public record FriendOnSale(
        long id,
        int price,
        long sellerId,
        long friendId
) {
}
