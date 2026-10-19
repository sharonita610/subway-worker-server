package com.game.subwayworkerserver.controller.request.stage;

public record DungeonRequest(
        String stationName,
        String dungeonUuId,
        boolean success
) {
}
