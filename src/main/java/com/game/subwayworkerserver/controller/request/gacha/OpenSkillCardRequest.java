package com.game.subwayworkerserver.controller.request.gacha;

public record OpenSkillCardRequest (
        String skillCardType,
        String payCondition,
        int price
) {
}
