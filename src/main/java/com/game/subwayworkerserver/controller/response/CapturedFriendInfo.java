package com.game.subwayworkerserver.controller.response;

public record CapturedFriendInfo(
        String friendName,
        String position,
        String baseHpRange,
        String baseManaRange,
        String maxManaRange,
        String baseAttackRange,
        String baseDefenseRange,
        int hp,
        int mana,
        int maxMana,
        int attack,
        int defense,
        String passiveSkills,
        String activeSkills
) {
}
