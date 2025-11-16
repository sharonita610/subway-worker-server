package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record EnabledSkills(
        List<SkillBaseInfo> activeSkills,
        List<SkillBaseInfo> passiveSkills
) {
    public static EnabledSkills of(CoworkerEntity coworker) {
        if (coworker.getCoworkerType().equals(CoworkerType.ENERZ)) {
            return new EnabledSkills(List.of(), List.of());
        }

        return new EnabledSkills(
                SkillBaseInfo.activeSkill(coworker.getActiveSkill1(), coworker.getActiveSkill2()),
                SkillBaseInfo.passiveSkill(coworker.getPassiveSkill1(), coworker.getPassiveSkill2())
        );
    }

    public static EnabledSkills of(CoworkerOnSale coworkerOnSale) {
        if (coworkerOnSale.type().equals(CoworkerType.ENERZ)) {
            return new EnabledSkills(List.of(), List.of());
        }
        return new EnabledSkills(
                SkillBaseInfo.activeSkill(coworkerOnSale.activeSkillId1(), coworkerOnSale.activeSkillId2()),
                SkillBaseInfo.passiveSkill(coworkerOnSale.passiveSkillId1(), coworkerOnSale.passiveSkillId2())
        );
    }
}
