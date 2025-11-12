package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum StageReward {

    // 스테이지 레벨 범위 (1~5)
    LEVEL_1_5(1, 5, 50, 25, 10000, 1, 0, 2500, 13, 7, 0.05, 0.10),

    // 스테이지 레벨 범위 (6~10)
    LEVEL_6_10(6, 10, 80, 40, 30000, 2, 50, 7500, 20, 10, 0.06, 0.12),

    // 스테이지 레벨 범위 (11~20)
    LEVEL_11_20(11, 20, 100, 50, 50000, 3, 70, 12500, 25, 13, 0.07, 0.14),

    // 스테이지 레벨 범위 (21~30)
    LEVEL_21_30(21, 30, 150, 75, 100000, 5, 100, 25000, 38, 19, 0.08, 0.16),

    // 스테이지 레벨 범위 (31~40)
    LEVEL_31_40(31, 40, 200, 100, 200000, 5, 300, 40000, 50, 25, 0.10, 0.20);


    // ----------------------------------------------------
    // 경험치 및 기본 정보
    // ----------------------------------------------------
    private final int minLevel;          // 최소 스테이지 레벨
    private final int maxLevel;          // 최대 스테이지 레벨

    // 최초 클리어 경험치 (개인/클리어한 동료 모두)
    private final int personalExp;
    private final int partnerExp;

    // 재 클리어 경험치 (반올림 *1/4)
    private final int rePersonalExp;
    private final int rePartnerExp;


    // ----------------------------------------------------
    // 최초 클리어 보상 (100%)
    // ----------------------------------------------------
    private final int firstCoin;                 // 코인
    private final int gachaTicketCount;          // 럭키가챠 티켓 개수
    private final int vacationTicketCount;        // 휴가 티켓 개수


    // ----------------------------------------------------
    // 반복 클리어 보상 (돈만 *1/4)
    // ----------------------------------------------------
    private final int reCoin;

    // ----------------------------------------------------
    // 추가 보상 (공통)
    // ----------------------------------------------------
    private final double treasureDropRate;       // 보물 드롭 확률 (if 문 보물)
    private final double gachaTicketDropRate;  // 보물 상자 뽑기 티켓 확률 (else 문 보물 상자 뽑기 티켓)


    /**
     * Enum 생성자
     */
    StageReward(
            int minLevel, int maxLevel,
            int personalExp, int partnerExp,
            int firstCoin, int gachaTicketCount, int vacationTicketCount,
            int reCoin,int rePersonalExp, int rePartnerExp,
            double treasureDropRate, double gachaTicketDropRate
    ) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.personalExp = personalExp;
        this.partnerExp = partnerExp;
        this.firstCoin = firstCoin;
        this.gachaTicketCount = gachaTicketCount;
        this.vacationTicketCount = vacationTicketCount;
        this.reCoin = reCoin;
        this.rePersonalExp = rePersonalExp;
        this.rePartnerExp = rePartnerExp;
        this.treasureDropRate = treasureDropRate;
        this.gachaTicketDropRate = gachaTicketDropRate;
    }

    public static Optional<StageReward> getRewardByLevel(int level) {
        return Arrays.stream(StageReward.values())
                .filter(reward -> level >= reward.minLevel && level <= reward.maxLevel)
                .findFirst();
    }
}