package com.game.subwayworkerserver.controller.request.gacha;

public record OpenLuckyGachaRequest (
        String payCondition,
        int price
) {
}
