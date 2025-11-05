package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record CoworkerListResponse(
        List<CoworkerBaseInfo> cowokerList
) {
    public static CoworkerListResponse of(final List<CoworkerEntity> coworkerEntityList) {
        List<CoworkerBaseInfo> coworkerBaseInfos = coworkerEntityList.stream()
                .map(CoworkerBaseInfo::of)
                .toList();
        return new CoworkerListResponse(coworkerBaseInfos);
    }

    public static CoworkerListResponse empty() {
        return new CoworkerListResponse(List.of());
    }
}
