package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.FriendAttribute;
import com.game.subwayworkerserver.domain.baseinfo.FriendLevel;

public record CaptureAvailableFriendResponse(
        String friendId,
        String friendName,
        String friendPosition,
        int friendLevel
) {

    public static CaptureAvailableFriendResponse of(
            final String friendId, final FriendAttribute friend, final FriendLevel level
    ) {
        return new CaptureAvailableFriendResponse(friendId, friend.getKoreanName(), level.getTitle(), level.getLevel());
    }

    public static CaptureAvailableFriendResponse of(
            final String friendId, final String name, final String position, final int level
    ) {
        return new CaptureAvailableFriendResponse(friendId, name, position, level);
    }
}
