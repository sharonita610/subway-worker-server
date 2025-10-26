package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record RandomGpsResponse(
        List<ItemLocationResponse> friends,
        List<ItemLocationResponse> giftBoxes
) {
}
