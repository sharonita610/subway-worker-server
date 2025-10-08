package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

public record SkillOnSaleResponse(
        List<SkillCardOnSale> skillCardOnSaleList
) {
}
