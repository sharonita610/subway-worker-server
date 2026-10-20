package com.game.subwayworkerserver.dto.response.item;


import com.game.subwayworkerserver.domain.item.ItemName;

public record CaptureItemInfo(
        String engName,
        String korName,
        int quantity
) {
    public static CaptureItemInfo of(String korName, int quantity) {
        return new CaptureItemInfo(
                String.valueOf(ItemName.fromKoreanName(korName)),
                korName,
                quantity
        );
    }
}
