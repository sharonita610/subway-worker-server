package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.StageRewardInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StageClearResponse(
        int userExp,
        int coworkerExp,
        int coin,
        int gachaTicket,
        int vacationTicket,
        ExtraReward extraReward
) {
    public static StageClearResponse of(final StageRewardInfo reward) {
        return new StageClearResponse(
                reward.userExp(),
                reward.coworkerExp(),
                reward.coin(),
                reward.gachaTicket(),
                reward.vacationTicket(),
                ExtraReward.of(reward.extraReward().name(), reward.extraReward().quantity())
        );
    }

    public static StageClearResponse empty() {
        return new StageClearResponse(0,0,0,0,0, null);
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
