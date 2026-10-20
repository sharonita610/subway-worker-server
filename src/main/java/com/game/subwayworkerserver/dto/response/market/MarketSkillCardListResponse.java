package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

/** 전역 장터와 내 판매 목록이 같은 형태를 쓴다 */
public record MarketSkillCardListResponse(List<MarketSkillCardInfo> skillCardList) {

    public static MarketSkillCardListResponse of(List<SkillCardOnSale> cards) {
        return new MarketSkillCardListResponse(cards.stream().map(MarketSkillCardInfo::of).toList());
    }
}
