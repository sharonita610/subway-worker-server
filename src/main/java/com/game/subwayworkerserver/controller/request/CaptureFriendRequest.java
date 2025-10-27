package com.game.subwayworkerserver.controller.request;

public record CaptureFriendRequest(
        String friendId,
        String captureItemName
) {

}
