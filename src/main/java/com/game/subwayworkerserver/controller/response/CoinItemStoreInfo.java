package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.Coin;

import java.util.List;

public record CoinItemStoreInfo(
        String name,
        String description,
        int price
) {
    public static List<CoinItemStoreInfo> of(List<Coin> coinList) {
        return coinList.stream().map(itemName ->
                        new CoinItemStoreInfo(itemName.name(), itemName.getDescription(), itemName.getTicketPrice())
                ).toList();
    }
}
