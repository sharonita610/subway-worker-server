package com.game.subwayworkerserver.dto.request.stage;

public record DungeonRequest(
        String stationName,
        String dungeonUuId,
        boolean success
) {
}
