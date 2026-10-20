package com.game.subwayworkerserver.dto.response.market;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.dto.response.coworker.CoworkerBaseInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MarketCoworkerInfo(
        int price,
        Long sellerId,
        long marketId,
        Boolean isPaid,
        Boolean isSold,
        CoworkerBaseInfo coworkerBaseInfo
) {
    public static MarketCoworkerInfo onSale(CoworkerOnSale coworker) {
        return new MarketCoworkerInfo(coworker.price(), coworker.sellerId(), coworker.marketId(), null, null, CoworkerBaseInfo.fromDomain(coworker));
    }

    public static MarketCoworkerInfo mine(CoworkerOnSale coworker) {
        return new MarketCoworkerInfo(coworker.price(), null, coworker.marketId(), coworker.isPaid(), coworker.isSold(), CoworkerBaseInfo.fromDomain(coworker));
    }
}
