package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum CurrencyType {
    COIN("coin"),
    VACATION_TICKET("vacationTicket"),
    CASH("cash"),
    FREE("free"),
    GACHA_TICKET("gachaTicket");

    private final String currencyType;

    CurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public static CurrencyType confirm(String currencyType) {
        String upperCaseType = currencyType.toUpperCase();

        if (upperCaseType.equals(VACATION_TICKET.name())) {
            return VACATION_TICKET;
        }

        if (upperCaseType.equals(GACHA_TICKET.name())) {
            return GACHA_TICKET;
        }

        throw new BaseException(
                ErrorCode.BAD_REQUEST, "유효하지 않은 재화 타입입니다. 'vacationTicket' 또는 'gachaTicket' 만 허용됩니다."
        );
    }
}
