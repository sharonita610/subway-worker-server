package com.game.subwayworkerserver.controller.response.user;

public record UuidResponse(
        String uuid
) {
    public static UuidResponse empty() {
        return new UuidResponse("");
    }
}
