package com.game.subwayworkerserver.controller.request;

public record SkillByUserRequest(
        long coworkerId,
        String cardType
) {
}
