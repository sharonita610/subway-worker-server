package com.game.subwayworkerserver.domain.item;

public record CaptureItem(
        String name,
        String description,
        int level,
        int price,
        int quantity
) {
}
