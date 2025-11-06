package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
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
        ExpInfo exp,
        StatsInfos statList,
        EnabledSkills enabledSkills,
        SelectedSkills selectedSkills

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
                .korElement(coworkerOnSale.element())
                .isPartner(coworkerOnSale.inUse())
                .onSale(coworkerOnSale.onSale())
                .exp(ExpInfo.of(coworkerOnSale.level(), coworkerOnSale.exp(), coworkerOnSale.totalExp()))
                .statList(StatsInfos.of(coworkerOnSale))
                .enabledSkills(EnabledSkills.of(coworkerOnSale))
                .selectedSkills(SelectedSkills.of(coworkerOnSale))
                .build();

    }

    public static CoworkerBaseInfo fromEntity(CoworkerEntity coworker, List<UserGachaEntity> userGachaEntities) {
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
                .statList(StatsInfos.of(coworker, userGachaEntities))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(SelectedSkills.of(coworker))
                .build();
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
                .selectedSkills(SelectedSkills.of(coworker))
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
            StatBaseInfo mana,
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
            StatBaseInfo manaInfo = StatBaseInfo.of(coworker.getMana(), 0L);
            StatBaseInfo maxManaInfo = StatBaseInfo.of(coworker.getMaxMana(), 0L);
            StatBaseInfo attackInfo = StatBaseInfo.of(coworker.getAttack(), attackBonus);
            StatBaseInfo defenseInfo = StatBaseInfo.of(coworker.getDefense(), defenseBonus);
            StatBaseInfo manaRecoveryInfo = StatBaseInfo.of(coworker.getManaRecovery(), manaRecoveryBonus);

            return new StatsInfos(hpInfo, manaInfo, maxManaInfo, attackInfo, defenseInfo, manaRecoveryInfo);
        }

        public static StatsInfos of(final CoworkerOnSale coworkerOnSale) {

            return new StatsInfos(
                    StatBaseInfo.of(coworkerOnSale.hp(), 0L),
                    StatBaseInfo.of(coworkerOnSale.mana(), 0L),
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
}