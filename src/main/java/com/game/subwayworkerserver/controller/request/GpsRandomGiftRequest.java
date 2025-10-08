package com.game.subwayworkerserver.controller.request;

public record GpsRandomGiftRequest(
        double latitude,   // 위도 (예: 37.508544)
        double longitude
) {
}
