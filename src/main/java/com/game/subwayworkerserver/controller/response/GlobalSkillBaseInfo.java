package com.game.subwayworkerserver.controller.response;

public record GlobalSkillBaseInfo(
        long id,
        int price,
        long sellerId,
        boolean sold,
        String name,
        int level
) {
}
