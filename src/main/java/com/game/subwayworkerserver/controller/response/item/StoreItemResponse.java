package com.game.subwayworkerserver.controller.response.item;

import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.List;

public record StoreItemResponse(
        List<TrapItemStoreInfo> trapItemList,
        List<CaptureItemStoreInfo> captureItemList,
        List<CoinItemStoreInfo> coinItemInfoList,
        List<VacationTicketStoreInfo> vacationTicketList
) {
    public static StoreItemResponse of(
            List<ItemName> trapList,
            int todayRandomTrapPurchase,
            List<ItemName> captureList,
            int todayRandomCapture,
            List<Coin> coinList,
            List<VacationTicket> vacationTicketList,
            boolean isFirst
    ) {

        return new StoreItemResponse(
                TrapItemStoreInfo.of(trapList, todayRandomTrapPurchase),
                CaptureItemStoreInfo.of(captureList, todayRandomCapture),
                CoinItemStoreInfo.of(coinList),
                VacationTicketStoreInfo.of(vacationTicketList, isFirst)
        );
    }
}
