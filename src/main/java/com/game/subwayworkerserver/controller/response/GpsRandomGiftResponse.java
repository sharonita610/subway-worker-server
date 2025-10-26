package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record GpsRandomGiftResponse(
        List<DefaultGiftInfo> randomGiftList,
        List<BonusGiftInfo> bonusGiftInfo
) {
    private record DefaultGiftInfo(
        String name,
        int quantity
    ) {
    }

    private record BonusGiftInfo(
            String name,
            String koreanName,
            String description,
            int quantity
    ) {

    }
}
