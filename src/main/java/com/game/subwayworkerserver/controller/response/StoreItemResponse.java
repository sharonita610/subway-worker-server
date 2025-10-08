package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record StoreItemResponse(
        List<TrapItemStoreInfo> trapItemList,
        List<CaptureItemStoreInfo> captureItemList,
        List<CoinItemInfo> coinItemInfoList,
        List<TreasureTicketInfo> treasureTicketList
) {
}
