package com.game.subwayworkerserver.dto.request.gps;

public record GpsAddTrapRequest(
        double latitude,   // 위도 (예: 37.508544)
        double longitude,
        String trapName
) {
}
