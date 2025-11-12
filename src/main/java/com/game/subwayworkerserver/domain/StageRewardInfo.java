package com.game.subwayworkerserver.domain;

public record StageRewardInfo(
        int userExp,
        int coworkerExp,
        int coworkersTotalExp,
        int coin,
        int gachaTicket,
        int vacationTicket,
        ExtraReward extraReward
) {

    private record ExtraReward(
            String name,
            int quantity
    ) {
    }
}
