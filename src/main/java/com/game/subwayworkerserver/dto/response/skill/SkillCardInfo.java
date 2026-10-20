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

/**
 * 스킬 카드 한 장을 보여주는 공통 record. 용도에 따라 채우는 필드가 다르고, 비운 필드는 JSON에서 빠진다.
 * - 스킬 정의만: id, engName, korName, description (동료가 쓸 수 있는 스킬 목록)
 * - 보유 카드: + level, requiredCount, actualCount, cardType (가챠로 얻은 카드)
 * - 장터 카드: + skillCardEnumId, inUse, onSale, available, isPaid, isSold
 */
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

    /** 동료가 가진 액티브 스킬 두 개. id 0은 NONE으로 풀린다. */
    public static List<SkillCardInfo> activeSkills(int first, int second) {
        return Stream.of(ActiveSkillCard.from(first), ActiveSkillCard.from(second)).map(SkillCardInfo::of).toList();
    }

    public static List<SkillCardInfo> passiveSkills(int first, int second) {
        return Stream.of(PassiveSkillCard.from(first), PassiveSkillCard.from(second)).map(SkillCardInfo::of).toList();
    }

    /** 보유 카드 요약: 정의 정보와 현재 레벨 */
    public static SkillCardInfo summary(SkillCardEntity card) {
        return SkillCardInfo.builder()
                .id(card.getId()).engName(card.getEngName()).korName(card.getKorName())
                .description(describe(card.getType(), card.getSkillCardEnumId()))
                .level(card.getCurrentLevel())
                .build();
    }

    /** 가챠로 얻은 카드: 레벨과 강화 진행도까지 */
    public static SkillCardInfo earned(SkillCardEntity card) {
        return SkillCardInfo.builder()
                .engName(card.getEngName()).korName(card.getKorName())
                .description(describe(card.getType(), card.getSkillCardEnumId()))
                .level(card.getCurrentLevel()).requiredCount(card.getRequiredCount()).actualCount(card.getCount())
                .cardType(card.getType().name())
                .build();
    }

    /** 장터에 올라온 카드: 거래 상태까지 */
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
