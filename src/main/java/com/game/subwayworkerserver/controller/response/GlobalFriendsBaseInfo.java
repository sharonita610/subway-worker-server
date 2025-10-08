package com.game.subwayworkerserver.controller.response;

public record GlobalFriendsBaseInfo(
        long id,
        String name,
        String position,
        int level,
        int price,
        long sellerId,
        boolean sold,
        int hp,
        int mana,
        int maxMana,
        int attack,
        int defense,
        String passiveSkills,
        String activeSkills
) {
}
