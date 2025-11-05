package com.game.subwayworkerserver.controller.response;

public record PaymentSuccessResponse(
        int sellingPrice,
        int commissionPrice,
        int profit
) {
    public static PaymentSuccessResponse of(int price, int commission, int coworkerRealPrice) {
        return new PaymentSuccessResponse(price, commission, coworkerRealPrice);
    }
}
