package com.game.subwayworkerserver.dto.request.gacha;

public record OpenLuckyGachaRequest (
        String payCondition,
        int price
) {
}
