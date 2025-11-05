package com.game.subwayworkerserver.controller.request;

public record SellingRequest(
        long coworkerId,
        int sellingPrice,
        int sellingCommission,
        int registrationFee
) {
}
