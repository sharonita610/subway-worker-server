package com.game.subwayworkerserver.controller.response.coworker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoworkerListResponse(
        BaseReward reward,
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
        return new CoworkerListResponse(null, coworkerBaseInfos);
    }

    public static CoworkerListResponse empty() {
        return new CoworkerListResponse(null, List.of());
    }

    public static CoworkerListResponse of(int coin, int exp, List<CoworkerEntity> coworkerEntityList) {
        List<CoworkerBaseInfo> coworkerBaseInfos = coworkerEntityList.stream()
                .map(CoworkerBaseInfo::fromEntity)
                .toList();
        return new CoworkerListResponse(new BaseReward(coin, exp), coworkerBaseInfos);
    }
}
