package com.game.subwayworkerserver.controller.response;


import com.game.subwayworkerserver.entity.ItemName;

public record CaptureItemInfo(
        String engName,
        String korName,
        int quantity
) {
    public static CaptureItemInfo of(String korName, int quantity) {
        return new CaptureItemInfo(
                String.valueOf(ItemName.fromConstantName(korName)),
                korName,
                quantity
        );
    }
}
