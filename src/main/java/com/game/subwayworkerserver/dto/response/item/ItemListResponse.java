package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.Comparator;
import java.util.List;

public record ItemListResponse(List<ItemInfo> itemList) {

    public static ItemListResponse of(List<ItemInfo> items) {
        return new ItemListResponse(items.stream()
                .sorted(Comparator.comparingInt(item -> ItemName.valueOf(item.engName()).ordinal()))
                .toList());
    }
}
