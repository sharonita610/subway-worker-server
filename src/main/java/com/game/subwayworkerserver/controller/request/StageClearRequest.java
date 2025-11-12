package com.game.subwayworkerserver.controller.request;

public record StageClearRequest(
        boolean success,
        int level
) {
}
