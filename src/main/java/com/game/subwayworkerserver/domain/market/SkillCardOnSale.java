package com.game.subwayworkerserver.domain.market;

public record SkillCardOnSale(
        long id,
        int price,
        boolean sold,
        int level,
        String name
) {
}

