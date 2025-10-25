package com.game.subwayworkerserver.controller.response;

import lombok.Builder;

public record FriendBaseInfo(
        long id,
        boolean inUse,
        boolean onSale,
        long hp,
        long mana,
        long maxMana,
        long attack,
        long defense,
        String passiveSkills,
        String activeSkills
) {
    @Builder
    public FriendBaseInfo {
    }
}
