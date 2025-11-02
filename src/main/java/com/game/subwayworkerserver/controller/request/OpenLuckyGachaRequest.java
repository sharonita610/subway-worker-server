package com.game.subwayworkerserver.controller.request;

public record OpenLuckyGachaRequest (
        String payCondition,
        int price
) {
}
