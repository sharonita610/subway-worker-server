package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.CoworkerEntity;

public record UserUpgradeResponse(
        CoworkerBaseInfo coworker
) {
    public static UserUpgradeResponse of(CoworkerEntity upgraded) {
        return new UserUpgradeResponse(
                CoworkerBaseInfo.fromEntity(upgraded)
        );
    }
}
