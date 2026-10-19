package com.game.subwayworkerserver.domain.stage;

import lombok.Builder;

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

    @Builder
    public StageRewardInfo {
    }

    public record ExtraReward(
            String name,
            int quantity
    ) {
    }
}
