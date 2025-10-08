package com.game.subwayworkerserver.domain.market;

public record FriendOnSale(
        long id,
        int price,
        long sellerId,
        long friendId,
        long buyerId,
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
