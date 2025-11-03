package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;

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
            new RewardConfig("BRIEFCASE", 10),
            new RewardConfig("BUSINESS_CARRIER", 5),
            new RewardConfig("SILVER_SHOVEL", 10),
            new RewardConfig("GOLD_SHOVEL", 5),
            new RewardConfig("EXTRA_FRIEND", 3),
            new RewardConfig("TREASURE", 10)
    );

    private record RewardConfig(String type, int chancePercent) {
    }

    public static List<DefaultGift> getDefaultReward() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        long coinRange = COIN_MAX - COIN_MIN;
        double skewedRandomCoin = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        long coins = (long) (COIN_MIN + skewedRandomCoin * coinRange);

        int ticketRange = TICKET_MAX - TICKET_MIN;
        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN + skewedRandomTicket * ticketRange);

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
                    rewards.add(new ExtraGift(treasure.getItemName(), 1));

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