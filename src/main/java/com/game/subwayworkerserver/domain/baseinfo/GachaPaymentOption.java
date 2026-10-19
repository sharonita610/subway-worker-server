package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.baseinfo.CurrencyType;
import com.game.subwayworkerserver.exception.BaseException;
import lombok.Getter;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

@Getter
public enum GachaPaymentOption {

    GACHA_PULL(1, 1, CurrencyType.GACHA_TICKET, "가챠티켓 1회 가챠"),
    SINGLE_PULL(1, 60, CurrencyType.VACATION_TICKET, "휴가 티켓 1회 가챠"),
    SIX_PULL(6, 300, CurrencyType.VACATION_TICKET, "휴가 티켓 5+1회 가챠");

    GachaPaymentOption(int count, int price, CurrencyType currencyType, String description) {
        this.count = count;
        this.price = price;
        this.currencyType = currencyType;
        this.description = description;
    }

    private final int count;
    private final int price;
    private final CurrencyType currencyType;
    private final String description;


    public static GachaPaymentOption confirmCountAndPrice(final CurrencyType paymentOption, final int price) {
        for (GachaPaymentOption option : values()) {
            if (option.currencyType == paymentOption && option.price == price) {
                return option;
            }
        }

        throw new BaseException(
                BAD_REQUEST, String.format("유효하지 않은 가챠 구매 옵션입니다. 타입: %s, 가격: %d", paymentOption.name(), price)
        );
    }
}
