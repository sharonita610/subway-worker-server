package com.game.subwayworkerserver.controller.request;

public record SellingRequest(
        long id,
        int ticketPrice,
        int cash
) {
}
