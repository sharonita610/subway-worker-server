package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record GlobalCoworkerResponse(
        List<GlobalCoworkerInfo> coworkerList
) {
    public static GlobalCoworkerResponse of(final List<CoworkerOnSale> filteredCoworkerList) {
        return new GlobalCoworkerResponse(
                filteredCoworkerList.stream()
                        .map(GlobalCoworkerInfo::of)
                        .toList()
        );
    }
}
