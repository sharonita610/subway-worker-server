package com.game.subwayworkerserver.dto.response.item;


import java.util.List;

public record BagpackInfo(
        List<TrapItemInfo> trapItemList,
        List<CaptureItemInfo> captureItemList
) {
}
