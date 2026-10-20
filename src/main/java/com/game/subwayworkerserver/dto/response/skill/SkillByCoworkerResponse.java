package com.game.subwayworkerserver.dto.response.skill;

import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record SkillByCoworkerResponse(
        List<SkillCard> skillCardList
) {
    public static SkillByCoworkerResponse of(
            final List<SkillCardEntity> skillCardEntityList, long coworkerId
    ) {
        return new SkillByCoworkerResponse(
                SkillCard.of(skillCardEntityList, coworkerId)
        );
    }
}
