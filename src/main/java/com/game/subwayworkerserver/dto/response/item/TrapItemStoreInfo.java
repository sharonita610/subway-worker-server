package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.item.ItemName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public record TrapItemStoreInfo(
        String engName,
        String korName,
        String description,
        int price,
        Integer viewedCount,
        Integer limitViewCount
) {
    public static List<TrapItemStoreInfo> of(List<ItemName> trapList, int todayRandomTrapPurchase) {
        return trapList.stream()
                .map(item -> {
                    String constantName = item.name().toUpperCase();
                    if (constantName.equals("RANDOM_TRAP")) {
                        return new TrapItemStoreInfo(
                                String.valueOf(item),
                                item.getName(),
                                item.getDescription(),
                                item.getPrice(),
                                todayRandomTrapPurchase,
                                3
                        );
                    } else {
                        return new TrapItemStoreInfo(
                                String.valueOf(item),
                                item.getName(),
                                item.getDescription(),
                                item.getPrice(),
                                null,
                                null
                        );
                    }
                })
                .toList();
    }
}