package com.game.subwayworkerserver.controller.response;


public record CaptureItemInfo(
        String name,
        int quantity
) {
    public static CaptureItemInfo of(String item, int quantity) {
        return new CaptureItemInfo(item, quantity);
    }
}
