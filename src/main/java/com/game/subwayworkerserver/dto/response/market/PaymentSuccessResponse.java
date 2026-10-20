package com.game.subwayworkerserver.dto.response.market;

public record PaymentSuccessResponse(
        int sellingPrice,
        int commissionPrice,
        int profit
) {
    public static PaymentSuccessResponse of(int price, int commission, int coworkerRealPrice) {
        return new PaymentSuccessResponse(price, commission, coworkerRealPrice);
    }
}
