package com.game.subwayworkerserver.controller.response.coworker;

import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record UserUpgradeResponse(
        CoworkerBaseInfo coworker
) {
    public static UserUpgradeResponse of(CoworkerEntity upgraded, List<SkillCardEntity> coworkerSkills) {
        return new UserUpgradeResponse(
                CoworkerBaseInfo.of(upgraded, coworkerSkills)
        );
    }
}
