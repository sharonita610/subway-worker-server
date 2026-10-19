package com.game.subwayworkerserver.domain.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 보상 공통 계산. 선물상자(GiftBoxService)와 가챠(GachaRandomGiftGenerator)가 같은 공식을 쓴다.
 */
public final class RewardRoller {

    /** 코인은 낮은 값 쪽으로 강하게 치우친다. */
    public static final double COIN_SKEW_FACTOR = 6.0;
    /** 티켓은 코인보다 완만하게 치우친다. */
    public static final double TICKET_SKEW_FACTOR = 3.5;

    private RewardRoller() {
    }

    /** 당첨 확률(%)이 붙은 보상 후보. */
    public record Chance(String name, int percent) {
    }

    /** [min, max) 구간에서 skew 지수만큼 아래쪽으로 치우친 값을 뽑는다. 지수가 클수록 min 근처가 많이 나온다. */
    public static long skewedBetween(long min, long max, double skew) {
        double skewed = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), skew);
        return (long) (min + skewed * (max - min));
    }

    /** 후보마다 독립적으로 주사위를 굴려 당첨된 이름을 후보 순서대로 돌려준다. */
    public static List<String> rollEach(List<Chance> chances) {
        List<String> hits = new ArrayList<>();
        for (Chance chance : chances) {
            if (ThreadLocalRandom.current().nextDouble(0, 100) < chance.percent()) {
                hits.add(chance.name());
            }
        }
        return hits;
    }
}
