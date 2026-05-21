package com.game.subwayworkerserver.controller.response.coworker;

import com.game.subwayworkerserver.controller.response.skill.SkillBaseInfo;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record EnabledSkills(
        List<SkillBaseInfo> activeSkills,
        List<SkillBaseInfo> passiveSkills
) {
    public static EnabledSkills of(CoworkerEntity coworker) {
        return new EnabledSkills(
                SkillBaseInfo.activeSkill(coworker.getActiveSkill1(), coworker.getActiveSkill2()),
                SkillBaseInfo.passiveSkill(coworker.getPassiveSkill1(), coworker.getPassiveSkill2())
        );
    }

    public static EnabledSkills of(CoworkerOnSale coworkerOnSale) {
        return new EnabledSkills(
                SkillBaseInfo.activeSkill(coworkerOnSale.activeSkillId1(), coworkerOnSale.activeSkillId2()),
                SkillBaseInfo.passiveSkill(coworkerOnSale.passiveSkillId1(), coworkerOnSale.passiveSkillId2())
        );
    }
}
