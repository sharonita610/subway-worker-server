package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import lombok.Builder;

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

    public static CoworkerBaseInfo from(CoworkerOnSale coworkerOnSale) {
        return CoworkerBaseInfo.builder()
                .id(coworkerOnSale.coworkerId())
                .engName(coworkerOnSale.engName())
                .korName(coworkerOnSale.korName())
                .position(coworkerOnSale.position())
                .korElement(coworkerOnSale.element())
                .isPartner(coworkerOnSale.inUse())
                .onSale(coworkerOnSale.onSale())
                .exp(ExpInfo.of(coworkerOnSale.level()))
                .statList(StatsInfos.of(coworkerOnSale))
                .enabledSkills(EnabledSkills.of(coworkerOnSale))
                .selectedSkills(SelectedSkills.of(coworkerOnSale))
                .build();

    }

    public static CoworkerBaseInfo of(CoworkerEntity coworker) {
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
                .statList(StatsInfos.of(coworker))
                .enabledSkills(EnabledSkills.of(coworker))
                .selectedSkills(SelectedSkills.of(coworker))
                .build();
    }

    private record StatBaseInfo(
            int power, int treasure, int total
    ) {
    }

    private record StatsInfos(
            StatBaseInfo hp,
            StatBaseInfo mana,
            StatBaseInfo maxMana,
            StatBaseInfo attack,
            StatBaseInfo defense,
            StatBaseInfo manaRecovery
    ) {
        public static StatsInfos of(CoworkerEntity coworker) {

            return null;
        }

        public static StatsInfos of(CoworkerOnSale coworkerOnSale) {

            return null;
        }
    }

    private record ExpInfo(
            int level, int now, int max
    ) {
        public static ExpInfo of(CoworkerEntity coworkerEntity) {

            return null;
        }

        public static ExpInfo of(int level) {
            return new ExpInfo(level, 0, 0);
        }
    }
}
