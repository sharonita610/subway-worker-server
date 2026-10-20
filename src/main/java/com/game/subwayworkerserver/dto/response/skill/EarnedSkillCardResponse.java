package com.game.subwayworkerserver.dto.response.skill;

import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record EarnedSkillCardResponse(
        List<SkillCardInfo> skillCardList
) {
    public static EarnedSkillCardResponse of(List<SkillCardEntity> randomCardList) {
        return new EarnedSkillCardResponse(randomCardList.stream().map(SkillCardInfo::earned).toList());
    }
}
