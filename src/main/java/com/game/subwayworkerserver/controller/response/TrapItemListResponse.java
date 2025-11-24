package com.game.subwayworkerserver.controller.response;

import java.util.Comparator;
import java.util.List;

public record TrapItemListResponse(List<TrapItemInfo> trapItemInfoList) {

    private static final List<String> TRAP_ORDER = List.of(
            "나무삽",
            "은삽",
            "금삽"
    );

    public static TrapItemListResponse of(List<TrapItemInfo> list) {
        Comparator<TrapItemInfo> customOrderComparator = Comparator.comparingInt(
                itemInfo -> TRAP_ORDER.indexOf(itemInfo.korName())
        );

        list.sort(customOrderComparator);
        return new TrapItemListResponse(list);
    }
}
