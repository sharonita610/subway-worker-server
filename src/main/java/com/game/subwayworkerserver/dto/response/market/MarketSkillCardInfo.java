package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.dto.response.skill.SkillCardInfo;

/** 장터의 스킬카드 한 건 */
public record MarketSkillCardInfo(
        int price,
        long sellerId,
        long marketId,
        SkillCardInfo skillCardBaseInfo
) {
    public static MarketSkillCardInfo of(SkillCardOnSale card) {
        return new MarketSkillCardInfo(card.price(), card.sellerId(), card.marketId(), SkillCardInfo.onSale(card));
    }
}
