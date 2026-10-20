package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.Comparator;
import java.util.List;

/** 포획 도구 또는 트랩 도구 목록. ItemName 선언 순서(하급 → 상급)로 정렬한다. */
public record ItemListResponse(List<ItemInfo> itemList) {

    public static ItemListResponse of(List<ItemInfo> items) {
        return new ItemListResponse(items.stream()
                .sorted(Comparator.comparingInt(item -> ItemName.valueOf(item.engName()).ordinal()))
                .toList());
    }
}
