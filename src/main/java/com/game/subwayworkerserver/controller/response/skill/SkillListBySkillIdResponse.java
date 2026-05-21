package com.game.subwayworkerserver.controller.response.skill;

import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record SkillListBySkillIdResponse(
        List<SkillCard> skillCardList

) {
    public static SkillListBySkillIdResponse of(List<SkillCardEntity> skillCardList) {
        return new SkillListBySkillIdResponse(
                SkillCard.of(skillCardList)
        );
    }
}
