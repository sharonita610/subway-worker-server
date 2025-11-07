package com.game.subwayworkerserver.controller.request;

public record SellingRequest(
        long targetId,
        int sellingPrice,
        int sellingCommission,
        int registrationFee
) {
}
