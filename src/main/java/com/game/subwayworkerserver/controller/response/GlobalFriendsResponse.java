package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.FriendOnSale;

import java.util.List;

public record GlobalFriendsResponse(
        List<GlobalFriendInfo> friendList
) {
    public static GlobalFriendsResponse of(final List<FriendOnSale> filteredFriends) {
        return new GlobalFriendsResponse(
                filteredFriends.stream()
                        .map(GlobalFriendInfo::of)
                        .toList()
        );
    }
}
