package com.game.subwayworkerserver.controller.response;

public record GlobalFriendInfo(

        FriendBaseInfo friendBaseInfo,
        int price,
        long sellerId,
        boolean sold,
        long buyerId
) {
}
