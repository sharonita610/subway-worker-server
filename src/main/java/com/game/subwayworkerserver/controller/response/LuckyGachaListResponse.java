package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;

import java.util.List;

public record LuckyGachaListResponse(
        int gotchaTicket,
        int vacationTicket,
        List<GachaBaseInfo> gachaList
) {
    public static LuckyGachaListResponse of(final UserEntity user, final List<UserGachaEntity> gachaEntityList) {
        return new LuckyGachaListResponse(
                user.getGotchaTicket(),
                user.getVacationTicket(),
                gachaEntityList.stream()
                        .map(GachaBaseInfo::of)
                        .toList()
        );
    }
}
