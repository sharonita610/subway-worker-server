package com.game.subwayworkerserver.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.item.ItemName;

/**
 * 보상·선물 한 항목. 이름과 수량은 항상 있고, 한글 이름과 설명은 아이템일 때만 채운다.
 * 레벨업 보상, 스테이지 추가 보상, 가챠 결과, 선물상자 결과가 모두 이 형태를 쓴다.
 */
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

    /** 아이템 상수명(또는 GACHA_TICKET)으로 한글 이름과 설명까지 채운다. */
    public static RewardInfo ofItem(String constantName, int quantity) {
        if (GACHA_TICKET.equals(constantName)) {
            return new RewardInfo(GACHA_TICKET, "가챠티켓", "가챠티켓", quantity);
        }
        ItemName item = ItemName.fromConstantName(constantName);
        return new RewardInfo(item.name(), item.getName(), item.getDescription(), quantity);
    }
}
