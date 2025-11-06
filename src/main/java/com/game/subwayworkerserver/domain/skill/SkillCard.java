package com.game.subwayworkerserver.domain.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SkillCard(
        long id,
        String engName,
        String korName,
        String description,
        int level,
        int requiredCount,
        int actualCount,
        boolean inUse,
        boolean thisCoworkerUsing,
        boolean otherCoworkerUsing,
        boolean onSale,
        long coworkerId,
        String cardType

) {

    @Builder
    public SkillCard {
    }

    public static List<SkillCard> of(final List<SkillCardEntity> skillCardEntityList) {

        return skillCardEntityList.stream()
                .map(card -> {

                    String description = getDescription(card);

                    return SkillCard.builder()
                            .id(card.getId())
                            .engName(card.getEngName())
                            .korName(card.getKorName())
                            .description(description)
                            .level(card.getCurrentLevel())
                            .requiredCount(card.getRequiredCount())
                            .actualCount(card.getCount())
                            .inUse(card.isInUse())
                            .onSale(card.isOnSale())
                            .coworkerId(card.getCoworkerId())
                            .cardType(card.getType().name())
                            .build();
                })
                .toList();
    }

    public static List<SkillCard> of(final List<SkillCardEntity> skillCardEntityList, final long coworkerId) {
        return skillCardEntityList.stream()
                .map(card -> {

                    String description = getDescription(card);

                    boolean isUsing = card.isInUse();
                    long currentCardOwnerId = card.getCoworkerId();
                    boolean thisCoworker = isUsing && currentCardOwnerId == coworkerId;
                    boolean otherCoworker = isUsing && currentCardOwnerId != 0 && currentCardOwnerId != coworkerId;

                    return SkillCard.builder()
                            .id(card.getId())
                            .engName(card.getEngName())
                            .korName(card.getKorName())
                            .description(description)
                            .level(card.getCurrentLevel())
                            .requiredCount(card.getRequiredCount())
                            .actualCount(card.getCount())
                            .inUse(isUsing)
                            .onSale(card.isOnSale())
                            .otherCoworkerUsing(otherCoworker)
                            .thisCoworkerUsing(thisCoworker)
                            .coworkerId(card.getCoworkerId())
                            .cardType(card.getType().name())
                            .build();
                })
                .toList();

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
