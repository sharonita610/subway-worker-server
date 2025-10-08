package com.game.subwayworkerserver.controller.request;

public record CaptureFriendRequest(
        String friendName,
        String captureItemName,
        int friendLevel,
        double latitude,
        double longitude
) {

}
