package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record GlobalFriendsResponse(
        List<GlobalFriendInfo> friendList
) {
}
