package com.game.subwayworkerserver.domain.reward;

import lombok.Getter;

@Getter
public enum DungeonReward {

    SUCCESS_REWARD(100, 50, 30000, 2, 20);

    private final int userExp;
    private final int coworkerExp;
    private final int coin;
    private final int gacha;
    private final int vacation;

    DungeonReward(int userExp, int coworkerExp, int coin, int gacha, int vacation) {
        this.userExp = userExp;
        this.coworkerExp = coworkerExp;
        this.coin = coin;
        this.gacha = gacha;
        this.vacation = vacation;
    }
}
