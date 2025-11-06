package com.game.subwayworkerserver.controller.response;


import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record SkillByUserResponse(
        String type,
        List<SkillCard> skillCardList
) {
    public static SkillByUserResponse of(final SkillCardType skillCardType, final List<SkillCardEntity> skillList) {
        return new SkillByUserResponse(
                skillCardType.name(),
                SkillCard.of(skillList)
        );
    }
}
