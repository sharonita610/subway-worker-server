package com.game.subwayworkerserver.dto.response.user;

public record UuidResponse(
        String uuid
) {
    public static UuidResponse empty() {
        return new UuidResponse("");
    }
}
