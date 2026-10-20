package com.game.subwayworkerserver.dto.response.skill;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;

public record UpgradedSkillCardResponse(
        SkillCard skillCard
) {
    public static UpgradedSkillCardResponse of(final SkillCardEntity targetCard) {
        return new UpgradedSkillCardResponse(
                SkillCard.builder()
                        .id(targetCard.getId())
                        .engName(targetCard.getEngName())
                        .korName(targetCard.getKorName())
                        .description(getDescription(targetCard))
                        .level(targetCard.getCurrentLevel())
                        .requiredCount(targetCard.getRequiredCount())
                        .actualCount(targetCard.getCount())
                        .inUse(targetCard.isInUse())
                        .onSale(targetCard.isOnSale())
                        .coworkerId(targetCard.getCoworkerId() != null ? targetCard.getCoworkerId() : 0L)
                        .cardType(targetCard.getType().name())
                        .build());

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
