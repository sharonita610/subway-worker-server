package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.UserGachaEntity;

public record GachaBaseInfo(
        String engName,
        String korName,
        String description,
        int count,
        int maxCount
) {

    public static GachaBaseInfo of(final UserGachaEntity userGachaEntity) {
        return new GachaBaseInfo(
                userGachaEntity.getGacha().name(),
                userGachaEntity.getGacha().getItemName(),
                userGachaEntity.getGacha().getDescription(),
                userGachaEntity.getGachaCount(),
                20
        );
    }
}
