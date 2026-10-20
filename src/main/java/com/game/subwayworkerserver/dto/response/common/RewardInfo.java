package com.game.subwayworkerserver.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.item.ItemName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RewardInfo(
        String name,
        String koreanName,
        String description,
        int quantity
) {
    private static final String GACHA_TICKET = "GACHA_TICKET";

    public static RewardInfo of(String name, int quantity) {
        return new RewardInfo(name, null, null, quantity);
    }

    public static RewardInfo of(String name, String koreanName, int quantity) {
        return new RewardInfo(name, koreanName, null, quantity);
    }

    public static RewardInfo ofItem(String constantName, int quantity) {
        if (GACHA_TICKET.equals(constantName)) {
            return new RewardInfo(GACHA_TICKET, "가챠티켓", "가챠티켓", quantity);
        }
        ItemName item = ItemName.fromConstantName(constantName);
        return new RewardInfo(item.name(), item.getName(), item.getDescription(), quantity);
    }
}
