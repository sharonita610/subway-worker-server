package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.item.ItemName;

/** 보유 아이템 한 종류. 설명은 구매 결과처럼 필요할 때만 채운다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemInfo(
        String engName,
        String korName,
        String description,
        int quantity
) {
    public static ItemInfo of(ItemName item, int quantity) {
        return new ItemInfo(item.name(), item.getName(), null, quantity);
    }

    public static ItemInfo withDescription(ItemName item, int quantity) {
        return new ItemInfo(item.name(), item.getName(), item.getDescription(), quantity);
    }
}
