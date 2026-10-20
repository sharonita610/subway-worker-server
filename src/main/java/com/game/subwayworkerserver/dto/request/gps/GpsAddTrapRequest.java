package com.game.subwayworkerserver.dto.request.gps;

public record GpsAddTrapRequest(
        double latitude,
        double longitude,
        String trapName
) {
}
