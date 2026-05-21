package com.game.subwayworkerserver.controller.response.item;

import com.game.subwayworkerserver.entity.ItemName;
import com.fasterxml.jackson.annotation.JsonInclude; // Import 추가
import com.fasterxml.jackson.annotation.JsonInclude.Include; // Import 추가

import java.util.List;

@JsonInclude(Include.NON_NULL)
public record CaptureItemStoreInfo(
        String engName,
        String korName,
        String description,
        int price,
        Integer viewedCount,
        Integer limitViewCount
) {
    public static List<CaptureItemStoreInfo> of(List<ItemName> captureList, int todayRandomCapturePurchase) {
        return captureList.stream()
                .map(item -> {
                    String constantName = item.name().toUpperCase();

                    if (constantName.equals("RANDOM_CAPTURE")) {
                        return new CaptureItemStoreInfo(
                                String.valueOf(item),
                                item.getName(),
                                item.getDescription(),
                                item.getPrice(),
                                todayRandomCapturePurchase,
                                3
                        );
                    } else {
                        return new CaptureItemStoreInfo(
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