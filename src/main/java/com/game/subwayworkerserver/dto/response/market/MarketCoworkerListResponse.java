package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record MarketCoworkerListResponse(List<MarketCoworkerInfo> coworkerList) {

    public static MarketCoworkerListResponse onSale(List<CoworkerOnSale> coworkers) {
        return new MarketCoworkerListResponse(coworkers.stream().map(MarketCoworkerInfo::onSale).toList());
    }

    public static MarketCoworkerListResponse mine(List<CoworkerOnSale> coworkers) {
        return new MarketCoworkerListResponse(coworkers.stream().map(MarketCoworkerInfo::mine).toList());
    }
}
