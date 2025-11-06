package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record SkillByCoworkerResponse(
        SkillCardType skillCardType,
        List<SkillCardNameInfo> enabledSkills,
        List<SkillCard> skillCardList
) {
    public static SkillByCoworkerResponse of(
            SkillCardType skillCardType,
            List<Integer> requestedSkill,
            final List<SkillCardEntity> skillCardEntityList
    ) {
        return new SkillByCoworkerResponse(
                skillCardType,
                SkillCardNameInfo.of(skillCardType, requestedSkill),
                SkillCard.of(skillCardEntityList)

        );
    }

    private record SkillCardNameInfo(
            String korSkillName,
            String description
    ) {
        public static List<SkillCardNameInfo> of(SkillCardType skillCardType, List<Integer> requestedSkill) {
            if (skillCardType.equals(SkillCardType.ACTIVE)) {
                return requestedSkill.stream()
                        .map(skill -> {
                            ActiveSkillCard skillCard = ActiveSkillCard.from(skill);
                            return new SkillCardNameInfo(skillCard.getKoreanName(), skillCard.getDescription());
                        }).toList();
            } else if (skillCardType.equals(SkillCardType.PASSIVE)) {
                return requestedSkill.stream()
                        .map(skill -> {
                            PassiveSkillCard skillCard = PassiveSkillCard.from(skill);
                            return new SkillCardNameInfo(skillCard.getKoreanName(), skillCard.getDescription());
                        }).toList();
            }
            return java.util.Collections.emptyList();
        }
    }
}
