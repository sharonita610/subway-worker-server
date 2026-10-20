package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;

import java.util.List;

/**
 * 재화 묶음 상품(코인 팩, 휴가 티켓 팩). 첫 구매 상품에만 isFirstBuyOffer와 구매 가능 여부가 붙는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PackStoreInfo(
        String name,
        String description,
        int price,
        Boolean isFirstBuyOffer,
        Boolean isAvailable
) {
    public static List<PackStoreInfo> ofCoins(List<Coin> coins) {
        return coins.stream()
                .map(coin -> new PackStoreInfo(coin.name(), coin.getDescription(), coin.getTicketPrice(), null, null))
                .toList();
    }

    public static List<PackStoreInfo> ofVacationTickets(List<VacationTicket> tickets, boolean hasPurchasedBefore) {
        return tickets.stream()
                .map(ticket -> ticket.isFirstBuyOffer()
                        ? new PackStoreInfo(ticket.name(), ticket.getDescription(), ticket.getPriceKrw(), true, !hasPurchasedBefore)
                        : new PackStoreInfo(ticket.name(), ticket.getDescription(), ticket.getPriceKrw(), null, null))
                .toList();
    }
}
