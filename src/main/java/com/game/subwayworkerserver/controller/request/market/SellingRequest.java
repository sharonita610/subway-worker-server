package com.game.subwayworkerserver.controller.request.market;

public record SellingRequest(
        long targetId,
        int sellingPrice,
        int sellingCommission,
        int registrationFee
) {
}
