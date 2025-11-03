package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.ItemName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GachaRandomGiftGenerator {

    private static final long COIN_MIN = 10000;
    private static final long COIN_MAX = 200000;
    private static final int TICKET_MIN = 3;
    private static final int TICKET_MAX = 200;
    private static final double SKEW_FACTOR = 3.5; // 낮은 값에 치우친 분포를 위한 인자

    private static final List<RewardConfig> EXTRA_REWARDS_CONFIGS = List.of(
            new RewardConfig(ItemName.BRIEFCASE.name(), 10),
            new RewardConfig(ItemName.BUSINESS_CARRIER.name(), 5),
            new RewardConfig(ItemName.SILVER_SHOVEL.name(), 10),
            new RewardConfig(ItemName.GOLD_SHOVEL.name(), 5),
            new RewardConfig("EXTRA_FRIEND", 3),
            new RewardConfig("TREASURE", 10)
    );

    private record RewardConfig(String type, int chancePercent) {
    }

    public static List<DefaultGift> getDefaultReward() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        final long COIN_MIN_ADJUSTED = 15000;
        final long COIN_MAX_ADJUSTED = 150000;
        // ⭐ 오류 수정: MAX - MIN = 135000 ⭐
        final long COIN_RANGE = COIN_MAX_ADJUSTED - COIN_MIN_ADJUSTED;

        // 1. 0~1 사이의 난수를 생성하고 SKEW_FACTOR 제곱하여 0에 가깝게 만듭니다.
        double skewedRandomCoin = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), SKEW_FACTOR);
        // 2. 이 값을 범위에 적용하여 낮은 코인 값을 생성합니다.
        long coins = (long) (COIN_MIN_ADJUSTED + skewedRandomCoin * COIN_RANGE);

        // TICKET_RANGE 계산 오류 수정 및 조정된 값 사용
        final int TICKET_MIN_ADJUSTED = 5;
        final int TICKET_MAX_ADJUSTED = 100;
        // ⭐ 오류 수정: MAX - MIN = 95 ⭐
        final int TICKET_RANGE = TICKET_MAX_ADJUSTED - TICKET_MIN_ADJUSTED;

        // 2. 휴가티켓 계산
        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN_ADJUSTED + skewedRandomTicket * TICKET_RANGE);


        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));

        return defaultGiftList;
    }

    public static List<ExtraGift> getExtraReward() {
        List<ExtraGift> rewards = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (RewardConfig config : EXTRA_REWARDS_CONFIGS) {
            int requiredProbability = config.chancePercent();

            double roll = random.nextDouble(0, 100);

            if (roll < requiredProbability) {

                if (config.type().equals("TREASURE")) {
                    GachaTreasure treasure = GachaTreasure.getRandom();
                    rewards.add(new ExtraGift(treasure.name(), 1));

                } else {
                    rewards.add(new ExtraGift(config.type(), 1));
                }
            }
        }
        return rewards;
    }

    public record DefaultGift(String type, String displayName, int quantity) {
    }

    public record ExtraGift(String type, int quantity) {
    }
}