package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GachaRandomGiftGenerator {

    private static final int MAX_TREASURE_COUNT = 20;

    private static final String MATERIAL_COWORKER = "MATERIAL_COWORKER";
    private static final String TREASURE = "TREASURE";

    private static final double SKEW_FACTOR = 3.5;
    private static final double COIN_SKEW_FACTOR = 6.0;
    private static final long COIN_MIN_ADJUSTED = 10000;
    private static final long COIN_MAX_ADJUSTED = 150000;
    private static final int TICKET_MIN_ADJUSTED = 20;
    private static final int TICKET_MAX_ADJUSTED = 200;

    private static final List<RewardConfig> EXTRA_REWARDS_CONFIGS = List.of(
            new RewardConfig(ItemName.BRIEFCASE.name(), 30),
            new RewardConfig(ItemName.BUSINESS_CARRIER.name(), 10),
            new RewardConfig(ItemName.SILVER_SHOVEL.name(), 30),
            new RewardConfig(ItemName.GOLD_SHOVEL.name(), 10),
            new RewardConfig(MATERIAL_COWORKER, 10),
            new RewardConfig(TREASURE, 25)
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
        double skewedRandomCoin = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), COIN_SKEW_FACTOR);
        long coins = (long) (COIN_MIN_ADJUSTED + skewedRandomCoin * COIN_RANGE);

        final int TICKET_RANGE = TICKET_MAX_ADJUSTED - TICKET_MIN_ADJUSTED;
        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN_ADJUSTED + skewedRandomTicket * TICKET_RANGE);

        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));

        return defaultGiftList;
    }

    public static List<ExtraGift> getExtraReward(int count, Map<GachaTreasure, Integer> userTreasureCounts) {
        List<ExtraGift> rawRewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rawRewards.addAll(calculateSingleExtraReward(userTreasureCounts));
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

    private static List<ExtraGift> calculateSingleExtraReward(final Map<GachaTreasure, Integer> userTreasureCounts) {
        List<ExtraGift> rewards = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (RewardConfig config : EXTRA_REWARDS_CONFIGS) {
            int requiredProbability = config.chancePercent();

            double roll = random.nextDouble(0, 100);

            if (roll < requiredProbability) {

                if (config.type().equals(TREASURE)) {
                    GachaTreasure treasure = substituteRandomAvailableTreasure(userTreasureCounts);
                    rewards.add(new ExtraGift(treasure.name(), 1));

                } else {
                    rewards.add(new ExtraGift(config.type(), 1));
                }
            }
        }
        return rewards;
    }

    private static GachaTreasure substituteRandomAvailableTreasure(final Map<GachaTreasure, Integer> userTreasureCounts) {
        List<GachaTreasure> allTreasures = List.of(GachaTreasure.values());

        List<GachaTreasure> maxedOutTreasures = allTreasures.stream()
                .filter(treasure -> userTreasureCounts.getOrDefault(treasure, 0) == MAX_TREASURE_COUNT)
                .toList();

        if (maxedOutTreasures.size() == MAX_TREASURE_COUNT) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "모든 보물의 최대 보유 한도(20개)에 도달했습니다.");
        }

        return GachaTreasure.getRandom(maxedOutTreasures);
    }


    public record DefaultGift(String type, String displayName, int quantity) {
    }

    public record ExtraGift(String type, int quantity) {
    }
}