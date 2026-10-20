package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record MarketCoworkerListResponse(List<MarketCoworkerInfo> coworkerList) {

    /** 전역 장터: 영입 가능한 동료 */
    public static MarketCoworkerListResponse onSale(List<CoworkerOnSale> coworkers) {
        return new MarketCoworkerListResponse(coworkers.stream().map(MarketCoworkerInfo::onSale).toList());
    }

    /** 내가 내놓은 동료 */
    public static MarketCoworkerListResponse mine(List<CoworkerOnSale> coworkers) {
        return new MarketCoworkerListResponse(coworkers.stream().map(MarketCoworkerInfo::mine).toList());
    }
}
