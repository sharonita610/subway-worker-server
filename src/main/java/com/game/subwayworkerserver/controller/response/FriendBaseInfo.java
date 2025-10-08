package com.game.subwayworkerserver.controller.response;

public record FriendBaseInfo(
        long id,
        boolean inUse,
        boolean onSale,
        int hp,
        int mana,
        int maxMana,
        int attack,
        int defense,
        String passiveSkills,
        String activeSkills
) {
}
