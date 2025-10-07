package com.game.subwayworkerserver.controller.response;


import java.util.List;

public record BagpackInfo(
        List<TrapItemInfo> trapItemList,
        List<CaptureItemInfo> captureItemList
) {
}
