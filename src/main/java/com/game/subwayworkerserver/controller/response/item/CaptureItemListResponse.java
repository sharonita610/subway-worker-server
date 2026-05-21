package com.game.subwayworkerserver.controller.response.item;

import com.game.subwayworkerserver.controller.response.item.CaptureItemInfo;

import java.util.Comparator;
import java.util.List;

public record CaptureItemListResponse(List<CaptureItemInfo> captureItemList) {

    private static final List<String> CAPTURE_ORDER = List.of(
            "종이박스",
            "서류가방",
            "출장용캐리어"
    );

    public static CaptureItemListResponse of(List<CaptureItemInfo> list) {
        Comparator<CaptureItemInfo> customOrderComparator = Comparator.comparingInt(
                itemInfo -> CAPTURE_ORDER.indexOf(itemInfo.korName())
        );

        list.sort(customOrderComparator);
        return new CaptureItemListResponse(list);
    }
}
