package com.game.subwayworkerserver.controller.request;

public record UserCreateRequest(
        String username,
        String friendAttribute
) {
}
