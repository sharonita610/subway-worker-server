package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record FriendListResponse(
        List<FriendBaseInfo> friendList
) {
}
