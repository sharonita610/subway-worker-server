package com.game.subwayworkerserver.dto.response.skill;


import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record SkillByUserResponse(
        List<SkillCard> activeSkillCardList,
        List<SkillCard> passiveSkillCardList
) {
    public static SkillByUserResponse of(List<SkillCardEntity> activeSkillCards, List<SkillCardEntity> passiveSkillCards) {
        return new SkillByUserResponse(
                SkillCard.of(activeSkillCards),
                SkillCard.of(passiveSkillCards)
        );
    }
}
