package com.game.subwayworkerserver.dto.response.coworker;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.dto.response.skill.SkillCardInfo;
import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record EnabledSkills(
        List<SkillCardInfo> activeSkills,
        List<SkillCardInfo> passiveSkills
) {
    public static EnabledSkills of(CoworkerEntity coworker) {
        return new EnabledSkills(
                SkillCardInfo.activeSkills(coworker.getActiveSkill1(), coworker.getActiveSkill2()),
                SkillCardInfo.passiveSkills(coworker.getPassiveSkill1(), coworker.getPassiveSkill2())
        );
    }

    public static EnabledSkills of(CoworkerOnSale coworkerOnSale) {
        return new EnabledSkills(
                SkillCardInfo.activeSkills(coworkerOnSale.activeSkillId1(), coworkerOnSale.activeSkillId2()),
                SkillCardInfo.passiveSkills(coworkerOnSale.passiveSkillId1(), coworkerOnSale.passiveSkillId2())
        );
    }
}
