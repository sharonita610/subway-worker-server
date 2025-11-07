package com.game.subwayworkerserver.domain.market;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Builder
    public SkillCardOnSale {
    }

    public static List<SkillCardOnSale> toDomain(
            final List<SkillCardMarketEntity> marketSkillCardLists,
            final List<SkillCardEntity> skillCardOnSaleByCardIds
    ) {

        // 1. SkillCardEntity 목록을 Map<CardId, SkillCardEntity>으로 변환하여 빠른 조회를 준비합니다.
        Map<Long, SkillCardEntity> cardMap = skillCardOnSaleByCardIds.stream()
                .collect(Collectors.toMap(
                        SkillCardEntity::getId, // Key: SkillCardEntity의 ID
                        card -> card           // Value: SkillCardEntity 객체 자체
                ));

        // 2. SkillCardMarketEntity 목록을 순회하며 데이터 결합 및 DTO로 변환합니다.
        return marketSkillCardLists.stream()
                .map(market -> {
                    long cardId = market.getSkillCardId();
                    SkillCardEntity card = cardMap.get(cardId);

                    // 데이터가 누락되었거나 필터링 조건에 의해 제거된 경우 (null일 경우) 스킵
                    if (card == null) {
                        return null;
                    }

                    // 3. Builder를 사용하여 SkillCardOnSale 객체 생성
                    return SkillCardOnSale.builder()
                            // SkillCardMarketEntity (마켓 정보)
                            .marketId(market.getId())
                            .cardId(cardId)
                            .price(market.getPrice())
                            .sellerId(market.getSellerId())
                            .isPaid(market.isPaid())
                            .isSold(market.isSold())
                            .available(market.isAvailable())

                            // SkillCardEntity (상세 스킬 정보)
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