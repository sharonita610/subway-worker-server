package com.game.subwayworkerserver.domain.ranking;

import lombok.Getter;

@Getter
public enum Tier {
    //                                         (minScore, maxScore, expectedPercent, expectedCount)
    IRON(0, 799, 0.0, 0),                       // 799 이하는 아이언으로 간주 (0점부터 시작 가정)
    BRONZE(800, 999, 0.20, 100),
    SILVER(1000, 1199, 0.30, 150),
    GOLD(1200, 1399, 0.25, 125),
    PLATINUM(1400, 1599, 0.15, 75),
    DIAMOND(1600, 1799, 0.07, 35),
    MASTER(1800, Integer.MAX_VALUE, 0.03, 15);

    private final int minScore;
    private final int maxScore;
    private final double expectedPercent; // 예상 인원 (%)
    private final int expectedCount;      // 예상 인원 (명)

    Tier(int minScore, int maxScore, double expectedPercent, int expectedCount) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.expectedPercent = expectedPercent;
        this.expectedCount = expectedCount;
    }

    public static Tier determineTier(int score) {
        for (Tier tier : values()) {
            if (score >= tier.minScore && score <= tier.maxScore) {
                return tier;
            }
        }
        // 예외 발생은 이론적으로 불가능하지만, 방어적인 코딩을 위해 추가
        throw new IllegalArgumentException("유효하지 않은 점수입니다: " + score);
    }
}
