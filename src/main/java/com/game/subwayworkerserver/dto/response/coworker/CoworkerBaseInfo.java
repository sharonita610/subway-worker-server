package com.game.subwayworkerserver.dto.response.coworker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 동료 목록·장터·강화 결과에서 쓰는 동료 상세 정보. 식별 정보는 CoworkerSummary를 평탄하게 품는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record CoworkerBaseInfo(
        @JsonUnwrapped CoworkerSummary summary,
        boolean isPartner,
        boolean onSale,
        boolean isMaterial,
        ExpInfo exp,
        StatsInfos statList,
        EnabledSkills enabledSkills,
        List<SkillCard> selectedSkills
) {
    public static CoworkerBaseInfo fromDomain(CoworkerOnSale coworkerOnSale) {
        return CoworkerBaseInfo.builder()
                .summary(CoworkerSummary.of(coworkerOnSale))
                .isPartner(coworkerOnSale.inUse())
                .onSale(coworkerOnSale.onSale())
                .isMaterial(coworkerOnSale.isMaterial())
                .exp(ExpInfo.of(coworkerOnSale.level(), coworkerOnSale.exp(), coworkerOnSale.totalExp()))
                .statList(StatsInfos.of(coworkerOnSale))
                .enabledSkills(EnabledSkills.of(coworkerOnSale))
                .selectedSkills(List.of())
                .build();
    }

    /** 보물 보너스와 장착 스킬까지 포함한 전체 정보 */
    public static CoworkerBaseInfo fromEntity(CoworkerEntity coworker, List<UserGachaEntity> userGachaEntities, List<SkillCardEntity> skillCardEntityList) {
        return CoworkerBaseInfo.builder()
                .summary(CoworkerSummary.of(coworker))
                .isPartner(coworker.isInUse())
                .onSale(coworker.isOnSale())
                .isMaterial(coworker.isMaterial())
                .exp(ExpInfo.of(coworker))
                .statList(StatsInfos.of(coworker, userGachaEntities))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(getSelectedSkill(coworker.getId(), skillCardEntityList))
                .build();
    }

    public static CoworkerBaseInfo fromEntity(CoworkerEntity coworker) {
        return fromEntity(coworker, List.of(), List.of());
    }

    public static CoworkerBaseInfo of(CoworkerEntity coworker, List<SkillCardEntity> coworkerSkills) {
        return fromEntity(coworker, List.of(), coworkerSkills);
    }

    private static List<SkillCard> getSelectedSkill(final long coworkerId, List<SkillCardEntity> skillCardEntityList) {
        if (skillCardEntityList == null || skillCardEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        return skillCardEntityList.stream()
                .filter(card -> Objects.equals(coworkerId, card.getCoworkerId()))
                .filter(SkillCardEntity::isInUse)
                .map(card -> SkillCard.builder()
                        .id(card.getId())
                        .engName(card.getEngName())
                        .korName(card.getKorName())
                        .description(getDescription(card))
                        .level(card.getCurrentLevel())
                        .requiredCount(card.getRequiredCount())
                        .actualCount(card.getCount())
                        .inUse(card.isInUse())
                        .thisCoworkerUsing(true)
                        .otherCoworkerUsing(false)
                        .onSale(card.isOnSale())
                        .coworkerId(card.getCoworkerId() != null ? card.getCoworkerId() : 0L)
                        .cardType(card.getType().name())
                        .build())
                .toList();
    }

    private record StatBaseInfo(long defaultPower, long treasure, long total) {
        public static StatBaseInfo of(long defaultPower, long treasure) {
            return new StatBaseInfo(defaultPower, treasure, defaultPower + treasure);
        }
    }

    private record StatsInfos(
            StatBaseInfo hp,
            StatBaseInfo initMana,
            StatBaseInfo maxMana,
            StatBaseInfo attack,
            StatBaseInfo defense,
            StatBaseInfo manaRecovery
    ) {
        public static StatsInfos of(final CoworkerEntity coworker, List<UserGachaEntity> userGachaEntities) {
            Map<GachaTreasure, Long> bonusMap = userGachaEntities.stream()
                    .collect(Collectors.groupingBy(UserGachaEntity::getGacha, Collectors.summingLong(UserGachaEntity::getGachaCount)));
            return new StatsInfos(
                    StatBaseInfo.of(coworker.getHp(), bonusMap.getOrDefault(GachaTreasure.HP_INCREASE, 0L)),
                    StatBaseInfo.of(coworker.getInitMana(), 0L),
                    StatBaseInfo.of(coworker.getMaxMana(), 0L),
                    StatBaseInfo.of(coworker.getAttack(), bonusMap.getOrDefault(GachaTreasure.ATTACK_UP, 0L)),
                    StatBaseInfo.of(coworker.getDefense(), bonusMap.getOrDefault(GachaTreasure.DEFENSE_UP, 0L)),
                    StatBaseInfo.of(coworker.getManaRecovery(), bonusMap.getOrDefault(GachaTreasure.MP_RECOVERY_UP, 0L))
            );
        }

        public static StatsInfos of(final CoworkerOnSale coworkerOnSale) {
            return new StatsInfos(
                    StatBaseInfo.of(coworkerOnSale.hp(), 0L),
                    StatBaseInfo.of(coworkerOnSale.initMana(), 0L),
                    StatBaseInfo.of(coworkerOnSale.maxMana(), 0L),
                    StatBaseInfo.of(coworkerOnSale.attack(), 0L),
                    StatBaseInfo.of(coworkerOnSale.defense(), 0L),
                    StatBaseInfo.of(coworkerOnSale.manaRecovery(), 0L)
            );
        }
    }

    private record ExpInfo(int level, int now, int max, int total) {
        public static ExpInfo of(CoworkerEntity coworkerEntity) {
            return new ExpInfo(
                    coworkerEntity.getLevel().getLevel(),
                    coworkerEntity.getCurrentExp(),
                    coworkerEntity.getLevel().getRequiredExpToNextLevel(),
                    coworkerEntity.getTotalExp()
            );
        }

        public static ExpInfo of(int level, int exp, int total) {
            return new ExpInfo(level, exp, CoworkerLevel.of(level).getRequiredExpToNextLevel(), total);
        }
    }

    private static String getDescription(SkillCardEntity card) {
        return card.getType().equals(SkillCardType.ACTIVE)
                ? ActiveSkillCard.from(card.getSkillCardEnumId()).getDescription()
                : PassiveSkillCard.from(card.getSkillCardEnumId()).getDescription();
    }
}
