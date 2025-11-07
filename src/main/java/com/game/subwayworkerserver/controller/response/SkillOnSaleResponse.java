package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

public record SkillOnSaleResponse(
        List<GlobalSkillCardInfo> skillCardList
) {
    public static SkillOnSaleResponse of(final List<SkillCardOnSale> skillCards) {
        return new SkillOnSaleResponse(
                skillCards.stream()
                        .map(GlobalSkillCardInfo::of)
                        .toList()
        );
    }
}
