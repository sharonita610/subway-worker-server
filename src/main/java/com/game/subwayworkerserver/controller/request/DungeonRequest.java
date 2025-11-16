package com.game.subwayworkerserver.controller.request;

public record DungeonRequest(
        String stationName,
        String dungeonUuId,
        boolean success
) {
}
