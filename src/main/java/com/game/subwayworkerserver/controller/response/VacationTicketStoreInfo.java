package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.fasterxml.jackson.annotation.JsonInclude; // Import 추가
import com.fasterxml.jackson.annotation.JsonInclude.Include; // Import 추가

import java.util.List;

@JsonInclude(Include.NON_NULL)
public record VacationTicketStoreInfo(
        String name,
        String description,
        int price,
        Boolean isFirstBuyOffer,
        Boolean isAvailable
) {

    public static List<VacationTicketStoreInfo> of(List<VacationTicket> vacationTicketList, boolean hasPurchasedBefore) {
        return vacationTicketList.stream()
                .map(ticket -> {

                    if (ticket.isFirstBuyOffer()) {
                        boolean available = !hasPurchasedBefore;

                        return new VacationTicketStoreInfo(
                                ticket.name(),
                                ticket.getDescription(),
                                ticket.getPriceKrw(),
                                true,
                                available
                        );
                    } else {
                        return new VacationTicketStoreInfo(
                                ticket.name(),
                                ticket.getDescription(),
                                ticket.getPriceKrw(),
                                null,
                                null
                        );
                    }
                })
                .toList();
    }
}