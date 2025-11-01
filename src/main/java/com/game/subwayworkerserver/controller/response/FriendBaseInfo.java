package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriendBaseInfo(
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
        String engAttributeName,
        String korAttributeName
) {
    @Builder
    public FriendBaseInfo {
    }
}
