package com.game.subwayworkerserver.dto.response.item;

import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

                        if (!hasPurchasedBefore) {
                            return new VacationTicketStoreInfo(
                                    ticket.name(),
                                    ticket.getDescription(),
                                    ticket.getPriceKrw(),
                                    true, // isFirstBuyOffer
                                    true  // isAvailable: 구매 가능
                            );
                        }
                        else {
                            return new VacationTicketStoreInfo(
                                    ticket.name(),
                                    ticket.getDescription(),
                                    ticket.getPriceKrw(),
                                    true,  // isFirstBuyOffer
                                    false // isAvailable: 구매 불가능 (목록에 노출만 됨)
                            );
                        }
                    }

                    else {
                        return new VacationTicketStoreInfo(
                                ticket.name(),
                                ticket.getDescription(),
                                ticket.getPriceKrw(),
                                null,  // isFirstBuyOffer: null (JSON 제외)
                                null   // isAvailable: null (JSON 제외, 항상 구매 가능 가정)
                        );
                    }
                })
                .toList();
    }
}