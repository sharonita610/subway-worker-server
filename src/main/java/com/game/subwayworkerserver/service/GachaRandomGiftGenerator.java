package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.ItemName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GachaRandomGiftGenerator {

    private static final String EXTRA_FRIEND = "EXTRA_FRIEND";
    private static final String TREASURE = "TREASURE";

    private static final double SKEW_FACTOR = 3.5;
    private static final long COIN_MIN_ADJUSTED = 15000;
    private static final long COIN_MAX_ADJUSTED = 150000;
    private static final int TICKET_MIN_ADJUSTED = 5;
    private static final int TICKET_MAX_ADJUSTED = 100;

    private static final List<RewardConfig> EXTRA_REWARDS_CONFIGS = List.of(
            new RewardConfig(ItemName.BRIEFCASE.name(), 10),
            new RewardConfig(ItemName.BUSINESS_CARRIER.name(), 5),
            new RewardConfig(ItemName.SILVER_SHOVEL.name(), 10),
            new RewardConfig(ItemName.GOLD_SHOVEL.name(), 5),
            new RewardConfig(EXTRA_FRIEND, 3),
            new RewardConfig(TREASURE, 10)
    );

    private record RewardConfig(String type, int chancePercent) {
    }

    public static List<DefaultGift> getDefaultReward(final int count) {
        List<DefaultGift> rawRewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rawRewards.addAll(calculateSingleDefaultReward());
        }

        if (rawRewards.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> summedMap = rawRewards.stream()
                .collect(Collectors.groupingBy(
                        DefaultGift::type,
                        Collectors.summingInt(DefaultGift::quantity)
                ));

        return summedMap.entrySet().stream()
                .map(entry -> new DefaultGift(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    private static List<DefaultGift> calculateSingleDefaultReward() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        final long COIN_RANGE = COIN_MAX_ADJUSTED - COIN_MIN_ADJUSTED;
        double skewedRandomCoin = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), SKEW_FACTOR);
        long coins = (long) (COIN_MIN_ADJUSTED + skewedRandomCoin * COIN_RANGE);

        final int TICKET_RANGE = TICKET_MAX_ADJUSTED - TICKET_MIN_ADJUSTED;
        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN_ADJUSTED + skewedRandomTicket * TICKET_RANGE);

        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));

        return defaultGiftList;
    }

    public static List<ExtraGift> getExtraReward(int count) {
        List<ExtraGift> rawRewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rawRewards.addAll(calculateSingleExtraReward());
        }

        if (rawRewards.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> summedMap = rawRewards.stream()
                .collect(Collectors.groupingBy(
                        ExtraGift::type,
                        Collectors.summingInt(ExtraGift::quantity)
                ));

        return summedMap.entrySet().stream()
                .map(entry -> new ExtraGift(
                        entry.getKey(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    private static List<ExtraGift> calculateSingleExtraReward() {
        List<ExtraGift> rewards = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (RewardConfig config : EXTRA_REWARDS_CONFIGS) {
            int requiredProbability = config.chancePercent();

            double roll = random.nextDouble(0, 100);

            if (roll < requiredProbability) {

                if (config.type().equals(TREASURE)) {
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