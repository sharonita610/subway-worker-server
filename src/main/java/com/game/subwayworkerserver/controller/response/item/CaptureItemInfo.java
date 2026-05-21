package com.game.subwayworkerserver.controller.response.item;


import com.game.subwayworkerserver.entity.ItemName;

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
