package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.ItemName;

public record TrapItemInfo(
        String engName,
        String korName,
        int quantity
) {
    public static TrapItemInfo of(String korName, int quantity) {
        return new TrapItemInfo(
                String.valueOf(ItemName.fromConstantName(korName)),
                korName,
                quantity
        );
    }
}
