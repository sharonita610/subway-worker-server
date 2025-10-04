package com.game.subwayworkerserver.domain.market;

public record SkillCardOnSale(
        long id,
        int price,
        long sellerId,
        long skillCardId
) {
}

