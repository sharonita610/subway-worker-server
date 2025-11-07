package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;

import java.util.List;

public record CoworkerListResponse(
        List<CoworkerBaseInfo> coworkerList
) {
    public static CoworkerListResponse of(
            final List<CoworkerEntity> coworkerEntityList,
            final List<UserGachaEntity> userGachaEntityList,
            final List<SkillCardEntity> skillCardEntityList
    ) {
        List<CoworkerBaseInfo> coworkerBaseInfos = coworkerEntityList.stream()
                .map(entity -> CoworkerBaseInfo.fromEntity(entity, userGachaEntityList, skillCardEntityList))
                .toList();
        return new CoworkerListResponse(coworkerBaseInfos);
    }

    public static CoworkerListResponse empty() {
        return new CoworkerListResponse(List.of());
    }

    public static CoworkerListResponse of(List<CoworkerEntity> coworkerEntityList) {
        List<CoworkerBaseInfo> coworkerBaseInfos = coworkerEntityList.stream()
                .map((CoworkerEntity coworker) -> CoworkerBaseInfo.fromEntity(coworker))
                .toList();
        return new CoworkerListResponse(coworkerBaseInfos);
    }
}
