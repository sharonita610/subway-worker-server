package com.game.subwayworkerserver.service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class EnhanceConstants {

    private EnhanceConstants() {}

    public static final double EXTRA_MULTIPLIER = 1.2;
    public static final long CRIT_STANDARD_XP = 400;

    // 확률 테이블 (Multiplier, Probability_Max_Boundary)
    // 1부터 100 사이의 난수를 사용하여, 난수가 Boundary 이하일 경우 해당 Multiplier 적용
    private static final Map<Double, Integer> CRIT_PROBABILITY_MAP = Map.of(
            0.7, 5,   // 5%
            0.8, 15,  // 10% (5 + 10)
            0.9, 30,  // 15% (15 + 15)
            1.0, 60,  // 30% (30 + 30) -> 기준 100%
            1.2, 80,  // 20% (60 + 20)
            1.3, 90,  // 10% (80 + 10)
            1.5, 95   // 5% (90 + 5)
            // 96~100 (남은 5%)는 default 1.0을 적용하거나, 필요 시 별도 처리
    );

    public static double getCriticalMultiplier() {
        int roll = ThreadLocalRandom.current().nextInt(100) + 1;

        for (Map.Entry<Double, Integer> entry : CRIT_PROBABILITY_MAP.entrySet()) {
            if (roll <= entry.getValue()) {
                return entry.getKey();
            }
        }
        return 1.0;
    }
}