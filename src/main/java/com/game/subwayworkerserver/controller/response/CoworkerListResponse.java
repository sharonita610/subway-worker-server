package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record CoworkerListResponse(
        List<CoworkerBaseInfo> cowokerList
) {
    public static CoworkerListResponse of(final List<CoworkerEntity> coworkerEntityList) {
        List<CoworkerBaseInfo> coworkerBaseInfos = coworkerEntityList.stream()
                .map(coworker ->
                        CoworkerBaseInfo.builder()
//                                .id(coworker.getId())
//                                .engName(String.valueOf(coworker.getCoworkerType()))
//                                .korName(coworker.getName())
//                                .position(coworker.getLevel().getTitle())
//                                .level(coworker.getLevel().getLevel())
//                                .inUse(coworker.isInUse())
//                                .onSale(coworker.isOnSale())
//                                .hp(coworker.getHp())
//                                .mana(coworker.getMana())
//                                .maxMana(coworker.getMaxMana())
//                                .attack(coworker.getAttack())
//                                .defense(coworker.getDefense())
//                                .manaRecovery(coworker.getManaRecovery())
//                                .activeSkills(SkillBaseInfo.activeSkill(coworker.getActiveSkill1(), coworker.getActiveSkill2()))
//                                .passiveSkills(SkillBaseInfo.passiveSkill(coworker.getPassiveSkill1(), coworker.getPassiveSkill2()))
//                                .engElement(coworker.getCoworkerElement().name())
//                                .korElement(coworker.getCoworkerElement().getKoreanName())
                                .build()
                ).toList();
        return new CoworkerListResponse(coworkerBaseInfos);
    }

    public static CoworkerListResponse empty() {
        return new CoworkerListResponse(List.of());
    }
}
