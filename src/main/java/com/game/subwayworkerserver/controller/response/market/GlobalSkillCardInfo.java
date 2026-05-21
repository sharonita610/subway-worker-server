package com.game.subwayworkerserver.controller.response.market;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import lombok.Builder;

public record GlobalSkillCardInfo(
        int price,
        long sellerId,
        long marketId,
        SkillCardBaseInfo skillCardBaseInfo
) {

    public static GlobalSkillCardInfo of(final SkillCardOnSale skillCardOnSale) {
        return new GlobalSkillCardInfo(
                skillCardOnSale.price(),
                skillCardOnSale.sellerId(),
                skillCardOnSale.marketId(),
                SkillCardBaseInfo.builder()
                        .id(skillCardOnSale.cardId())
                        .korName(skillCardOnSale.korName())
                        .engName(skillCardOnSale.engName())
                        .description(skillCardOnSale.description())
                        .cardType(skillCardOnSale.cardType())
                        .currentLevel(skillCardOnSale.currentLevel())
                        .count(skillCardOnSale.count())
                        .requiredCount(skillCardOnSale.requiredCount())
                        .skillCardEnumId(skillCardOnSale.skillCardEnumId())
                        .isPaid(skillCardOnSale.isPaid())
                        .inUse(skillCardOnSale.inUse())
                        .onSale(skillCardOnSale.onSale())
                        .isSold(skillCardOnSale.isSold())
                        .available(skillCardOnSale.available())
                        .build()
        );
    }

    private record SkillCardBaseInfo(
            long id,
            String korName,
            String engName,
            String description,
            String cardType,
            int currentLevel,
            int count,
            int requiredCount,
            int skillCardEnumId,
            boolean isPaid,
            boolean inUse,
            boolean onSale,
            boolean isSold,
            boolean available
    ) {

        @Builder
        private SkillCardBaseInfo {
        }
    }
}
