package com.game.subwayworkerserver.controller.response;

import lombok.Builder;

import java.util.List;

public record FriendBaseInfo(
        long id,
        boolean inUse,
        boolean onSale,
        long hp,
        long mana,
        long maxMana,
        long attack,
        long defense,
        long manaRecovery,
        List<SkillBaseInfo> passiveSkills,
        List<SkillBaseInfo> activeSkills
) {
    @Builder
    public FriendBaseInfo {
    }
}
