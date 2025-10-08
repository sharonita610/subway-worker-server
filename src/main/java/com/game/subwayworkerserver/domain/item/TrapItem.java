package com.game.subwayworkerserver.domain.item;

public record TrapItem(
        String name,
        String description,
        int level,
        int price,
        int quantity
) {

}
