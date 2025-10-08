package com.game.subwayworkerserver.controller.request;

import com.game.subwayworkerserver.controller.response.CaptureItemInfo;

import java.util.List;

public record CaptureItemListResponse(
    List<CaptureItemInfo> captureItemList
) {
}
