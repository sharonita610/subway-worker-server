package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SubscriptionItem {

    SILVER_BADGE(1, "실버 뱃지", 8800, 30, 300, 30, 0),
    GOLD_BADGE(2, "골드 뱃지", 14850, 30, 700, 60, 30);

    private final int id;
    private final String korName;
    private final int price;
    private final int durationDays;

    private final int instanceTicket;
    private final int dailyTicket;
    private final int adSkipDays;

    SubscriptionItem(int id, String korName, int price, int durationDays, int instanceTicket, int dailyTicket, int adSkipDays) {
        this.id = id;
        this.korName = korName;
        this.price = price;
        this.durationDays = durationDays;
        this.instanceTicket = instanceTicket;
        this.dailyTicket = dailyTicket;
        this.adSkipDays = adSkipDays;
    }

    public static List<SubscriptionItem> getAll() {
        return Arrays.asList(SubscriptionItem.values());
    }

    public static SubscriptionItem getById(final long subscribeId) {
        for (SubscriptionItem item : values()) {
            if (item.id == subscribeId) {
                return item;
            }
        }
        throw new BaseException(ErrorCode.BAD_REQUEST, "유효하지 않은 구독 상품 ID입니다: " + subscribeId);
    }
}