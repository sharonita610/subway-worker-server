package com.game.subwayworkerserver.dto.response.market;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.dto.response.coworker.CoworkerBaseInfo;

/**
 * 장터의 동료 한 건. 전역 목록에는 판매자가, 내 판매 목록에는 정산 상태가 붙는다.
 */
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
