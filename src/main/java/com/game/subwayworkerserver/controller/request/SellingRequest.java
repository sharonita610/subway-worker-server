package com.game.subwayworkerserver.controller.request;

public record SellingRequest(
        long friendId,
        int sellingPrice,
        int sellingCommission,
        int registrationFee
) {
}
