package com.game.subwayworkerserver.domain.stage;

import lombok.Builder;

@Builder
public record StageRewardInfo(
        int userExp,
        int coworkerExp,
        int coworkersTotalExp,
        int coin,
        int gachaTicket,
        int vacationTicket,
        Boolean firstClear,
        ExtraReward extraReward
) {

    public record ExtraReward(
            String name,
            int quantity
    ) {
    }
}
