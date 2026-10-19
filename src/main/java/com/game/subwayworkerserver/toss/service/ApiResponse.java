package com.game.subwayworkerserver.toss.service;

public record ApiResponse<T>(
        int statusCode,

        String message,

        T data
) {
}
