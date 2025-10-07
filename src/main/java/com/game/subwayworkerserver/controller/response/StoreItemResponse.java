package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.item.CaptureItem;
import com.game.subwayworkerserver.domain.item.TrapItem;

import java.util.List;

public record StoreItemResponse(
        List<TrapItem> trapItemList,
        List<CaptureItem> captureItemList,
        List<CoinItem> coinItemList,
        List<TreasureTicket> treasureTicketList
) {
}
