package com.game.subwayworkerserver.controller.response;

public record DungeonResponse (
        boolean canAccess,
        String dungeonUuid
){
    public static DungeonResponse of(final boolean canAccess) {
        return new DungeonResponse(canAccess, "");
    }

    public static DungeonResponse of(final boolean canAccess, final String dungeonUuId) {
        return new DungeonResponse(canAccess, dungeonUuId);
    }
}
