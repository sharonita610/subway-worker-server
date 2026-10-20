package com.game.subwayworkerserver.dto.response.gacha;

import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;

import java.util.List;

public record LuckyGachaListResponse(
        int gachaTicket,
        int vacationTicket,
        List<GachaBaseInfo> gachaList
) {
    public static LuckyGachaListResponse of(final UserEntity user, final List<UserGachaEntity> gachaEntityList) {
        return new LuckyGachaListResponse(
                user.getGachaTicket(),
                user.getVacationTicket(),
                gachaEntityList.stream()
                        .map(GachaBaseInfo::of)
                        .toList()
        );
    }
}
