package com.game.subwayworkerserver.dto.request.gacha;

public record OpenSkillCardRequest (
        String skillCardType,
        String payCondition,
        int price
) {
}
