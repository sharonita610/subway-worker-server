package com.game.subwayworkerserver.dto.request.market;

public record SellingRequest(
        long targetId,
        int sellingPrice,
        int sellingCommission,
        int registrationFee
) {
}
