package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;

import java.util.List;

public record EarnedSkillCardResponse(
        List<SkillCardBaseInfo> skillCardList
) {
    public static EarnedSkillCardResponse of(List<SkillCardEntity> randomCardList) {
        return new EarnedSkillCardResponse(
                randomCardList.stream()
                        .map(SkillCardBaseInfo::of)
                        .toList()
        );
    }

    private record SkillCardBaseInfo(
            String engName,
            String korName,
            String description,
            int level,
            int requiredCount,
            int actualCount,
            String cardType
    ) {
        public static SkillCardBaseInfo of(SkillCardEntity skillCard) {
            String description = getDescription(skillCard);
            return new SkillCardBaseInfo(
                    skillCard.getEngName(),
                    skillCard.getKorName(),
                    description,
                    skillCard.getCurrentLevel(),
                    skillCard.getRequiredCount(),
                    skillCard.getCount(),
                    skillCard.getType().name()
            );
        }

    }

    private static String getDescription(SkillCardEntity card) {
        String description;
        if (card.getType().equals(SkillCardType.ACTIVE)) {
            ActiveSkillCard activeCard = ActiveSkillCard.from(card.getSkillCardEnumId());
            description = activeCard.getDescription();
        } else {
            PassiveSkillCard passiveCard = PassiveSkillCard.from(card.getSkillCardEnumId());
            description = passiveCard.getDescription();
        }
        return description;
    }
}
