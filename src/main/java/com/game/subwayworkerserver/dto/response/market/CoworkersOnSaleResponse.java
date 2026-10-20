package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record CoworkersOnSaleResponse(
        List<CoworkerOnSale> coworkerOnSaleList
) {
}
