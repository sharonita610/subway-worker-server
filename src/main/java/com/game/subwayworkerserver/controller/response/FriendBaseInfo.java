package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.FriendOnSale;
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

    public static FriendBaseInfo from(FriendOnSale friendOnSale) {
        return FriendBaseInfo.builder()
                .id(friendOnSale.friendId())
                .engName(friendOnSale.engName())
                .korName(friendOnSale.korName())
                .position(friendOnSale.position())
                .level(friendOnSale.level())
                .inUse(friendOnSale.inUse())
                .onSale(friendOnSale.onSale())
                .hp(friendOnSale.hp())
                .mana(friendOnSale.mana())
                .maxMana(friendOnSale.maxMana())
                .attack(friendOnSale.attack())
                .defense(friendOnSale.defense())
                .manaRecovery(friendOnSale.manaRecovery())
                .passiveSkills(SkillBaseInfo.passiveSkill(friendOnSale.passiveSkillId1(), friendOnSale.passiveSkillId2()))
                .activeSkills(SkillBaseInfo.activeSkill(friendOnSale.activeSkillId1(), friendOnSale.activeSkillId2()))
                .korAttributeName(friendOnSale.attribute())
                .build();
    }
}
