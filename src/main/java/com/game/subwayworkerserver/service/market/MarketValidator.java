package com.game.subwayworkerserver.service.market;

import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

class MarketValidator {

    private MarketValidator() {}

    static void checkUserCoin(final int registrationFee, final UserEntity user) {
        if (!user.hasCoinMoreThanItemPrice(registrationFee)) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }
    }

    static void checkUserVacationTicket(final UserEntity user, final int price) {
        if (!user.hasEnoughVacationTickets(price)) {
            throw new BaseException(BAD_REQUEST, "휴가티켓이 부족해요!");
        }
    }
}
