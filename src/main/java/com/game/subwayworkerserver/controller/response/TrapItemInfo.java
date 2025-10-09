package com.game.subwayworkerserver.controller.response;

public record TrapItemInfo(
        String name,
        int quantity
) {
    public static TrapItemInfo of(String name, int quantity) {
        return new TrapItemInfo(name, quantity);
    }
}
