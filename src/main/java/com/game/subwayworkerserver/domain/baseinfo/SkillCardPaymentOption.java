package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import lombok.Getter;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

@Getter
public enum SkillCardPaymentOption {

    ACTIVE_SINGLE_PULL(1, 60, CurrencyType.VACATION_TICKET, SkillCardType.ACTIVE),
    ACTIVE_SIX_PULL(6, 300, CurrencyType.VACATION_TICKET, SkillCardType.ACTIVE),
    PASSIVE_SINGLE_PULL(1, 60, CurrencyType.VACATION_TICKET, SkillCardType.PASSIVE),
    PASSIVE_SIX_PULL(6, 300, CurrencyType.VACATION_TICKET, SkillCardType.PASSIVE);

    SkillCardPaymentOption(int count, int price, CurrencyType currencyType, SkillCardType cardType) {
        this.count = count;
        this.price = price;
        this.currencyType = currencyType;
        this.cardType = cardType;
    }

    private final int count;
    private final int price;
    private final CurrencyType currencyType;
    private final SkillCardType cardType;

    public static SkillCardPaymentOption confirmCountAndPriceAndCardType(
            final CurrencyType paymentOption, final int price, final SkillCardType cardType) {
        for (SkillCardPaymentOption option : values()) {
            if (option.currencyType == paymentOption && option.price == price && option.cardType == cardType) {
                return option;
            }
        }

        throw new BaseException(
                BAD_REQUEST, String.format("유효하지 않은 스킬 구매 옵션입니다. 타입: %s, 가격: %d", paymentOption.name(), price)
        );
    }
}
