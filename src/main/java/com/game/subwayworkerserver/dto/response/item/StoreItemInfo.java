package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.List;

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
