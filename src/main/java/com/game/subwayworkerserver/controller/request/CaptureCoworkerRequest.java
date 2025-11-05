package com.game.subwayworkerserver.controller.request;

public record CaptureCoworkerRequest(
        String coworkerId,
        String captureItemName
) {

}
