package com.game.subwayworkerserver.controller.request;

import com.game.subwayworkerserver.domain.item.CaptureItem;

import java.util.List;

public record CaptureItemListResponse(
    List<CaptureItem> captureItemList
) {
}
