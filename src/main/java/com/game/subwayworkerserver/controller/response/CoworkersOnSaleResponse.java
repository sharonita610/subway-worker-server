package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record CoworkersOnSaleResponse(
        List<CoworkerOnSale> coworkerOnSaleList
) {
}
