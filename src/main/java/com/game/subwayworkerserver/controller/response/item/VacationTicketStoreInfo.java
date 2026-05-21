package com.game.subwayworkerserver.controller.response.item;

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

                    // 1. 첫 구매 혜택 상품인 경우
                    if (ticket.isFirstBuyOffer()) {

                        // 1-1. 아직 구매하지 않은 경우 (혜택 제공)
                        if (!hasPurchasedBefore) {
                            return new VacationTicketStoreInfo(
                                    ticket.name(),
                                    ticket.getDescription(),
                                    ticket.getPriceKrw(),
                                    true, // isFirstBuyOffer
                                    true  // isAvailable: 구매 가능
                            );
                        }
                        // 1-2. 이미 구매한 경우 (혜택 만료)
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

                    // 2. 일반 상품인 경우 (모두에게 항상 구매 가능)
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