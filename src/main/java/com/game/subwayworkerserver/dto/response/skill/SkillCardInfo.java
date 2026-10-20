package com.game.subwayworkerserver.dto.response.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import lombok.Builder;

import java.util.List;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record SkillCardInfo(
        Long id,
        String engName,
        String korName,
        String description,
        Integer level,
        Integer requiredCount,
        Integer actualCount,
        String cardType,
        Integer skillCardEnumId,
        Boolean inUse,
        Boolean onSale,
        Boolean available,
        Boolean isPaid,
        Boolean isSold
) {
    public static SkillCardInfo of(ActiveSkillCard skill) {
        return SkillCardInfo.builder()
                .id((long) skill.getId()).engName(skill.name()).korName(skill.getKoreanName()).description(skill.getDescription())
                .build();
    }

    public static SkillCardInfo of(PassiveSkillCard skill) {
        return SkillCardInfo.builder()
                .id((long) skill.getId()).engName(skill.name()).korName(skill.getKoreanName()).description(skill.getDescription())
                .build();
    }

    public static List<SkillCardInfo> activeSkills(int first, int second) {
        return Stream.of(ActiveSkillCard.from(first), ActiveSkillCard.from(second)).map(SkillCardInfo::of).toList();
    }

    public static List<SkillCardInfo> passiveSkills(int first, int second) {
        return Stream.of(PassiveSkillCard.from(first), PassiveSkillCard.from(second)).map(SkillCardInfo::of).toList();
    }

    public static SkillCardInfo summary(SkillCardEntity card) {
        return SkillCardInfo.builder()
                .id(card.getId()).engName(card.getEngName()).korName(card.getKorName())
                .description(describe(card.getType(), card.getSkillCardEnumId()))
                .level(card.getCurrentLevel())
                .build();
    }

    public static SkillCardInfo earned(SkillCardEntity card) {
        return SkillCardInfo.builder()
                .engName(card.getEngName()).korName(card.getKorName())
                .description(describe(card.getType(), card.getSkillCardEnumId()))
                .level(card.getCurrentLevel()).requiredCount(card.getRequiredCount()).actualCount(card.getCount())
                .cardType(card.getType().name())
                .build();
    }

    public static SkillCardInfo onSale(SkillCardOnSale card) {
        return SkillCardInfo.builder()
                .id(card.cardId()).engName(card.engName()).korName(card.korName()).description(card.description())
                .level(card.currentLevel()).requiredCount(card.requiredCount()).actualCount(card.count())
                .cardType(card.cardType()).skillCardEnumId(card.skillCardEnumId())
                .inUse(card.inUse()).onSale(card.onSale()).available(card.available())
                .isPaid(card.isPaid()).isSold(card.isSold())
                .build();
    }

    private static String describe(SkillCardType type, int enumId) {
        return type == SkillCardType.ACTIVE
                ? ActiveSkillCard.from(enumId).getDescription()
                : PassiveSkillCard.from(enumId).getDescription();
    }
}
