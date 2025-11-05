package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import lombok.Builder;

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

    public static CoworkerBaseInfo from(CoworkerOnSale coworkerOnSale) {
        return CoworkerBaseInfo.builder()
//                .id(coworkerOnSale.coworkerId())
//                .engName(coworkerOnSale.engName())
//                .korName(coworkerOnSale.korName())
//                .position(coworkerOnSale.position())
//                .level(coworkerOnSale.level())
//                .isPartner(coworkerOnSale.inUse())
//                .onSale(coworkerOnSale.onSale())
//                .hp(StatInfo.of(coworkerOnSale.hp())
//                .mana(coworkerOnSale.mana())
//                .maxMana(coworkerOnSale.maxMana())
//                .attack(coworkerOnSale.attack())
//                .defense(coworkerOnSale.defense())
//                .manaRecovery(coworkerOnSale.manaRecovery())
//                .passiveSkills(SkillBaseInfo.passiveSkill(coworkerOnSale.passiveSkillId1(), coworkerOnSale.passiveSkillId2()))
//                .activeSkills(SkillBaseInfo.activeSkill(coworkerOnSale.activeSkillId1(), coworkerOnSale.activeSkillId2()))
//                .korElement(coworkerOnSale.element())
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
    }

    private record ExpInfo(
            int level, int now, int max
    ) {
    }
}
