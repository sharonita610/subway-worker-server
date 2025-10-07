package com.game.subwayworkerserver.controller.request;

public record GpsRequest(
        double latitude,   // 위도 (예: 37.508544)
        double longitude,  // 경도 (예: 127.058358)
        double rangeKm     // 검색 반경 (단위: km)
) {}


