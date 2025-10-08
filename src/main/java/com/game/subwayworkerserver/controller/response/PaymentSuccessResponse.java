package com.game.subwayworkerserver.controller.response;

public record PaymentSuccessResponse(
        int soldPrice,
        int commission,
        int profit
) {
}
