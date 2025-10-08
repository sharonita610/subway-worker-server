package com.game.subwayworkerserver.controller.request;

public record CaptureFriendRequest(
        String friendName,
        int level,
        String captureItemName,
        double x,
        double y
) {

}
