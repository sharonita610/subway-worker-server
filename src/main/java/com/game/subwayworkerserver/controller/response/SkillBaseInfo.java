package com.game.subwayworkerserver.controller.response;

public record SkillBaseInfo(
        long id,
        String name,
        String description,
        int level,
        boolean inUse
) {
}
