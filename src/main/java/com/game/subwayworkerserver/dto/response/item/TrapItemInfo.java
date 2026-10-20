package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.item.ItemName;

public record TrapItemInfo(
        String engName,
        String korName,
        int quantity
) {
    public static TrapItemInfo of(String korName, int quantity) {
        return new TrapItemInfo(
                String.valueOf(ItemName.fromKoreanName(korName)),
                korName,
                quantity
        );
    }
}
