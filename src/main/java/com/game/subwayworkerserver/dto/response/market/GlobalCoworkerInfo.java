package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.dto.response.coworker.CoworkerBaseInfo;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

public record GlobalCoworkerInfo(
        int price,
        long sellerId,
        long marketId,
        CoworkerBaseInfo coworkerBaseInfo
) {
    public static GlobalCoworkerInfo of(final CoworkerOnSale coworkerOnSale) {
        return new GlobalCoworkerInfo(
                coworkerOnSale.price(),
                coworkerOnSale.sellerId(),
                coworkerOnSale.marketId(),
                CoworkerBaseInfo.fromDomain(coworkerOnSale)
        );
    }
}
