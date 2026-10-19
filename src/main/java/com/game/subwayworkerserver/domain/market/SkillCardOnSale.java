package com.game.subwayworkerserver.domain.market;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record SkillCardOnSale(
        long marketId,
        long cardId,
        String engName,
        String korName,
        String description,
        boolean inUse,
        boolean onSale,
        boolean available,
        int currentLevel,
        int requiredCount,
        int count,
        int skillCardEnumId,
        int price,
        long sellerId,
        boolean isPaid,
        boolean isSold,
        String cardType

) {

    public static List<SkillCardOnSale> toDomain(
            final List<SkillCardMarketEntity> marketSkillCardLists,
            final List<SkillCardEntity> skillCardOnSaleByCardIds
    ) {

        Map<Long, SkillCardEntity> cardMap = skillCardOnSaleByCardIds.stream()
                .collect(Collectors.toMap(
                        SkillCardEntity::getId,
                        card -> card
                ));

        return marketSkillCardLists.stream()
                .map(market -> {
                    long cardId = market.getSkillCardId();
                    SkillCardEntity card = cardMap.get(cardId);

                    // 데이터가 누락되었거나 필터링 조건에 의해 제거된 경우 (null일 경우) 스킵
                    if (card == null) {
                        return null;
                    }

                    return SkillCardOnSale.builder()
                            .marketId(market.getId())
                            .cardId(cardId)
                            .price(market.getPrice())
                            .sellerId(market.getSellerId())
                            .isPaid(market.isPaid())
                            .isSold(market.isSold())
                            .available(market.isAvailable())

                            // SkillCardEntity의 name 필드가 영어 이름이라고 가정
                            .engName(card.getEngName())
                            .korName(card.getKorName())
                            .description("d")
                            .currentLevel(card.getCurrentLevel())
                            .requiredCount(card.getRequiredCount())
                            .skillCardEnumId(card.getSkillCardEnumId())
                            .count(card.getCount())
                            .inUse(card.isInUse())
                            .onSale(card.isOnSale())
                            .cardType(card.getType().name())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}