package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoworkerBaseInfo(
        long id,
        String engName,
        String korName,
        String position,
        String engElement,
        String korElement,
        boolean isPartner,
        boolean onSale,
        boolean isMaterial,
        ExpInfo exp,
        StatsInfos statList,
        EnabledSkills enabledSkills,
        List<SkillCard> selectedSkills

) {
    @Builder
    public CoworkerBaseInfo {
    }

    public static CoworkerBaseInfo fromDomain(CoworkerOnSale coworkerOnSale) {
        return CoworkerBaseInfo.builder()
                .id(coworkerOnSale.coworkerId())
                .engName(coworkerOnSale.engName())
                .korName(coworkerOnSale.korName())
                .position(coworkerOnSale.position())
                .engElement(coworkerOnSale.engElement())
                .korElement(coworkerOnSale.korElement())
                .isPartner(coworkerOnSale.inUse())
                .onSale(coworkerOnSale.onSale())
                .isMaterial(coworkerOnSale.isMaterial())
                .exp(ExpInfo.of(coworkerOnSale.level(), coworkerOnSale.exp(), coworkerOnSale.totalExp()))
                .statList(StatsInfos.of(coworkerOnSale))
                .enabledSkills(EnabledSkills.of(coworkerOnSale))
                .selectedSkills(List.of())
                .build();

    }

    public static CoworkerBaseInfo fromEntity(
            CoworkerEntity coworker,
            List<UserGachaEntity> userGachaEntities,
            List<SkillCardEntity> skillCardEntityList
    ) {
        return CoworkerBaseInfo.builder()
                .id(coworker.getId())
                .engName(String.valueOf(coworker.getCoworkerType()))
                .korName(coworker.getName())
                .position(coworker.getLevel().getTitle())
                .engElement(coworker.getCoworkerElement().name())
                .korElement(coworker.getCoworkerElement().getKoreanName())
                .isPartner(coworker.isInUse())
                .onSale(coworker.isOnSale())
                .isMaterial(coworker.isMaterial())
                .exp(ExpInfo.of(coworker))
                .statList(StatsInfos.of(coworker, userGachaEntities))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(getSelectedSkill(coworker.getId(), skillCardEntityList))
                .build();
    }

    private static List<SkillCard> getSelectedSkill(final long coworkerId, List<SkillCardEntity> skillCardEntityList) {
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

    public static CoworkerBaseInfo fromEntity(CoworkerEntity coworker) {
        return CoworkerBaseInfo.builder()
                .id(coworker.getId())
                .engName(String.valueOf(coworker.getCoworkerType()))
                .korName(coworker.getName())
                .position(coworker.getLevel().getTitle())
                .engElement(coworker.getCoworkerElement().name())
                .korElement(coworker.getCoworkerElement().getKoreanName())
                .isPartner(coworker.isInUse())
                .onSale(coworker.isOnSale())
                .exp(ExpInfo.of(coworker))
                .statList(StatsInfos.of(coworker, List.of()))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(List.of())
                .build();
    }

    public static CoworkerBaseInfo of(CoworkerEntity coworker, List<SkillCardEntity> coworkerSkills) {
        return CoworkerBaseInfo.builder()
                .id(coworker.getId())
                .engName(String.valueOf(coworker.getCoworkerType()))
                .korName(coworker.getName())
                .position(coworker.getLevel().getTitle())
                .engElement(coworker.getCoworkerElement().name())
                .korElement(coworker.getCoworkerElement().getKoreanName())
                .isPartner(coworker.isInUse())
                .onSale(coworker.isOnSale())
                .exp(ExpInfo.of(coworker))
                .statList(StatsInfos.of(coworker, List.of()))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(getSelectedSkill(coworker.getId(), coworkerSkills))
                .build();

    }

    private record StatBaseInfo(
            long defaultPower, long treasure, long total
    ) {
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
                    .collect(Collectors.groupingBy(
                            UserGachaEntity::getGacha,
                            Collectors.summingLong(UserGachaEntity::getGachaCount)
                    ));

            long hpBonus = bonusMap.getOrDefault(GachaTreasure.HP_INCREASE, 0L);
            long attackBonus = bonusMap.getOrDefault(GachaTreasure.ATTACK_UP, 0L);
            long defenseBonus = bonusMap.getOrDefault(GachaTreasure.DEFENSE_UP, 0L);
            long manaRecoveryBonus = bonusMap.getOrDefault(GachaTreasure.MP_RECOVERY_UP, 0L);


            StatBaseInfo hpInfo = StatBaseInfo.of(coworker.getHp(), hpBonus);
            StatBaseInfo manaInfo = StatBaseInfo.of(coworker.getInitMana(), 0L);
            StatBaseInfo maxManaInfo = StatBaseInfo.of(coworker.getMaxMana(), 0L);
            StatBaseInfo attackInfo = StatBaseInfo.of(coworker.getAttack(), attackBonus);
            StatBaseInfo defenseInfo = StatBaseInfo.of(coworker.getDefense(), defenseBonus);
            StatBaseInfo manaRecoveryInfo = StatBaseInfo.of(coworker.getManaRecovery(), manaRecoveryBonus);

            return new StatsInfos(hpInfo, manaInfo, maxManaInfo, attackInfo, defenseInfo, manaRecoveryInfo);
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

    private record ExpInfo(
            int level, int now, int max, int total
    ) {
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