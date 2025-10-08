package com.game.subwayworkerserver.controller.response;

public record SkillBaseInfo(
        long id,
        String name,
        int level,
        boolean inUse
) {
}
