package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.List;

public record StoreItemResponse(
        List<StoreItemInfo> trapItemList,
        List<StoreItemInfo> captureItemList,
        List<PackStoreInfo> coinItemInfoList,
        List<PackStoreInfo> vacationTicketList
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
                StoreItemInfo.of(trapList, todayRandomTrapPurchase),
                StoreItemInfo.of(captureList, todayRandomCapture),
                PackStoreInfo.ofCoins(coinList),
                PackStoreInfo.ofVacationTickets(vacationTicketList, isFirst)
        );
    }
}
