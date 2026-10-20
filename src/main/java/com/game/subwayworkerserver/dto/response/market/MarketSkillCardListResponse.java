package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

public record MarketSkillCardListResponse(List<MarketSkillCardInfo> skillCardList) {

    public static MarketSkillCardListResponse of(List<SkillCardOnSale> cards) {
        return new MarketSkillCardListResponse(cards.stream().map(MarketSkillCardInfo::of).toList());
    }
}
