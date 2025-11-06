package com.game.subwayworkerserver.domain.skill;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import lombok.Builder;

import java.util.List;

public record SkillCard(
        long id,
        String engName,
        String korName,
        String description,
        int level,
        int maxLevel,
        int quantity,
        boolean inUse,
        boolean onSale,
        SkillCardType cardType

) {

    @Builder
    public SkillCard {
    }

    public static List<SkillCard> of(List<SkillCardEntity> skillCardEntityList) {

        return skillCardEntityList.stream()
                .map(card -> {

                    String description;
                    if (card.getType().equals(SkillCardType.ACTIVE)) {
                        ActiveSkillCard activeCard = ActiveSkillCard.from(card.getSkillCardEnumId());
                        description = activeCard.getDescription();
                    } else {
                        PassiveSkillCard passiveCard = PassiveSkillCard.from(card.getSkillCardEnumId());
                        description = passiveCard.getDescription();
                    }

                    return SkillCard.builder()
                            .id(card.getId())
                            .engName(card.getEngName())
                            .korName(card.getKorName())
                            .description(description)
                            .level(card.getCurrentLevel())
                            .maxLevel(card.getMaxLevel())
                            .quantity(card.getQuantity())
                            .inUse(card.isInUse())
                            .onSale(card.isOnSale())
                            .cardType(card.getType())
                            .build();
                })
                .toList();
    }
}
