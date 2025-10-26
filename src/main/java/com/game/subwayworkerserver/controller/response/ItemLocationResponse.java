package com.game.subwayworkerserver.controller.response;

public record ItemLocationResponse(
        String itemName,
        Double latitude,
        Double longitude,
        String feature,
        Double distanceKm
) {
}