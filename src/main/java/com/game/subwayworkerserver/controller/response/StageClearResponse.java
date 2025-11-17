package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.domain.reward.DungeonReward;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StageClearResponse(
        int userExp,
        int coworkerExp,
        int coin,
        int gachaTicket,
        int vacationTicket,
        Boolean firstClear,
        ExtraReward extraReward
) {
    public static StageClearResponse of(final StageRewardInfo reward) {
        return new StageClearResponse(
                reward.userExp(),
                reward.coworkerExp(),
                reward.coin(),
                reward.gachaTicket(),
                reward.vacationTicket(),
                reward.firstClear(),
                ExtraReward.of(reward.extraReward().name(), reward.extraReward().quantity())
        );
    }

    public static StageClearResponse empty() {
        return new StageClearResponse(0,0,0,0,0, null, null);
    }

    public static StageClearResponse of(DungeonReward reward) {
        return new StageClearResponse(
                reward.getUserExp(),
                reward.getCoworkerExp(),
                reward.getCoin(),
                reward.getGacha(),
                reward.getVacation(),
                null,
                null
        );
    }

    public record ExtraReward(
            String name,
            int quantity
    ) {
        public static ExtraReward of(String name, int quantity) {
            return new ExtraReward(name, quantity);
        }
    }
}
