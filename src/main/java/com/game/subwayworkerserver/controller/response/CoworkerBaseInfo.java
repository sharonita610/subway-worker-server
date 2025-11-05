package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoworkerBaseInfo(
        long id,
        String engName,
        String korName,
        String position,
        int level,
        boolean inUse,
        boolean onSale,
        long hp,
        long mana,
        long maxMana,
        long attack,
        long defense,
        long manaRecovery,
        List<SkillBaseInfo> passiveSkills,
        List<SkillBaseInfo> activeSkills,
        String engElement,
        String korElement
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
                .level(coworkerOnSale.level())
                .inUse(coworkerOnSale.inUse())
                .onSale(coworkerOnSale.onSale())
                .hp(coworkerOnSale.hp())
                .mana(coworkerOnSale.mana())
                .maxMana(coworkerOnSale.maxMana())
                .attack(coworkerOnSale.attack())
                .defense(coworkerOnSale.defense())
                .manaRecovery(coworkerOnSale.manaRecovery())
                .passiveSkills(SkillBaseInfo.passiveSkill(coworkerOnSale.passiveSkillId1(), coworkerOnSale.passiveSkillId2()))
                .activeSkills(SkillBaseInfo.activeSkill(coworkerOnSale.activeSkillId1(), coworkerOnSale.activeSkillId2()))
                .korElement(coworkerOnSale.element())
                .build();
    }
}
