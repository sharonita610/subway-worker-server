package com.game.subwayworkerserver.domain.skill;

public record SkillCard(
        long id,
        String name,
        boolean inUse,
        boolean onSale,
        SkillCardType type

) {
}
