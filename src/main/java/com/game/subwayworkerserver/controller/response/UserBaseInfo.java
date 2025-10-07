package com.game.subwayworkerserver.controller.response;

public record UserBaseInfo(
        long id,
        String username,
        int coin,
        int cash,
        int stageLevel
) {
}
