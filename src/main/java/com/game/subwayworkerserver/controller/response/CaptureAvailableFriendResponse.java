package com.game.subwayworkerserver.controller.response;

public record CaptureAvailableFriendResponse(
        String friendName,
        String friendPosition,
        int friendLevel
) {
}
