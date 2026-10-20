package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.List;

/** 상점의 도구 한 종류. 하루 한도가 있는 랜덤 도구(RANDOM_*)에만 오늘 구매 횟수와 한도를 붙인다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreItemInfo(
        String engName,
        String korName,
        String description,
        int price,
        Integer viewedCount,
        Integer limitViewCount
) {
    private static final String RANDOM_PREFIX = "RANDOM_";
    private static final int DAILY_RANDOM_LIMIT = 3;

    public static List<StoreItemInfo> of(List<ItemName> items, int todayRandomPurchase) {
        return items.stream()
                .map(item -> item.name().startsWith(RANDOM_PREFIX)
                        ? new StoreItemInfo(item.name(), item.getName(), item.getDescription(), item.getPrice(), todayRandomPurchase, DAILY_RANDOM_LIMIT)
                        : new StoreItemInfo(item.name(), item.getName(), item.getDescription(), item.getPrice(), null, null))
                .toList();
    }
}
