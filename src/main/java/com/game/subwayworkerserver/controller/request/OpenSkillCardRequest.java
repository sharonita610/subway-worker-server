package com.game.subwayworkerserver.controller.request;

public record OpenSkillCardRequest (
        String skillCardType,
        String payCondition,
        int price
) {
}
