package com.game.subwayworkerserver.controller.request.gps;

public record GpsRandomGiftRequest(
        double latitude,   // 위도 (예: 37.508544)
        double longitude
) {
}
