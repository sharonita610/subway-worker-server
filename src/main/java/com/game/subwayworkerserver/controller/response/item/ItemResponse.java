package com.game.subwayworkerserver.controller.response.item;

public record ItemResponse(String engName, String korName, String description, int quantity) {
    public static ItemResponse of(String engName, String korName, String description, int quantity) {
        return new ItemResponse(engName, korName, description, quantity);
    }
}
