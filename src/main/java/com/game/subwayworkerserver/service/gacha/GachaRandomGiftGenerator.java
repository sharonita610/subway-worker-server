package com.game.subwayworkerserver.service.gacha;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import com.game.subwayworkerserver.domain.reward.RewardRoller;
import com.game.subwayworkerserver.domain.reward.RewardRoller.Chance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 럭키 가챠의 기본 보상(코인·휴가티켓)과 추가 보상(도구·재료·보물)을 뽑는다.
 * 난수 공식은 RewardRoller를 쓴다.
 */
public class GachaRandomGiftGenerator {

    private static final int MAX_TREASURE_COUNT = 20;
    private static final String MATERIAL_COWORKER = "MATERIAL_COWORKER";
    private static final String TREASURE = "TREASURE";

    private static final long COIN_MIN = 10_000;
    private static final long COIN_MAX = 150_000;
    private static final long TICKET_MIN = 20;
    private static final long TICKET_MAX = 200;

    private static final List<Chance> EXTRA_REWARD_CHANCES = List.of(
            new Chance(ItemName.BRIEFCASE.name(), 30),
            new Chance(ItemName.BUSINESS_CARRIER.name(), 10),
            new Chance(ItemName.SILVER_SHOVEL.name(), 30),
            new Chance(ItemName.GOLD_SHOVEL.name(), 10),
            new Chance(MATERIAL_COWORKER, 10),
            new Chance(TREASURE, 25)
    );

    private GachaRandomGiftGenerator() {
    }

    /** count 번 뽑은 기본 보상을 종류별로 합산해 돌려준다. */
    public static List<DefaultGift> getDefaultReward(final int count) {
        Map<String, DefaultGift> summed = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            for (DefaultGift gift : rollDefaultReward()) {
                summed.merge(gift.name(), gift,
                        (a, b) -> new DefaultGift(a.name(), a.koreanName(), a.quantity() + b.quantity()));
            }
        }
        return new ArrayList<>(summed.values());
    }

    private static List<DefaultGift> rollDefaultReward() {
        int coins = (int) RewardRoller.skewedBetween(COIN_MIN, COIN_MAX, RewardRoller.COIN_SKEW_FACTOR);
        int tickets = (int) RewardRoller.skewedBetween(TICKET_MIN, TICKET_MAX, RewardRoller.TICKET_SKEW_FACTOR);
        return List.of(
                new DefaultGift("coin", "코인", coins),
                new DefaultGift("vacationTicket", "휴가티켓", tickets)
        );
    }

    /** count 번 뽑은 추가 보상을 종류별로 합산해 돌려준다. 보물은 보유 한도에 닿지 않은 것 중에서 고른다. */
    public static List<ExtraGift> getExtraReward(final int count, final Map<GachaTreasure, Integer> userTreasureCounts) {
        Map<String, Integer> summed = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            for (ExtraGift gift : rollExtraReward(userTreasureCounts)) {
                summed.merge(gift.name(), gift.quantity(), Integer::sum);
            }
        }
        return summed.entrySet().stream()
                .map(entry -> new ExtraGift(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static List<ExtraGift> rollExtraReward(final Map<GachaTreasure, Integer> userTreasureCounts) {
        List<ExtraGift> rewards = new ArrayList<>();
        for (String hit : RewardRoller.rollEach(EXTRA_REWARD_CHANCES)) {
            String name = hit.equals(TREASURE)
                    ? substituteRandomAvailableTreasure(userTreasureCounts).name()
                    : hit;
            rewards.add(new ExtraGift(name, 1));
        }
        return rewards;
    }

    /** 보유 한도(20개)에 도달한 보물을 제외하고 하나를 고른다. 남은 보물이 없으면 GachaTreasure가 예외를 던진다. */
    private static GachaTreasure substituteRandomAvailableTreasure(final Map<GachaTreasure, Integer> userTreasureCounts) {
        List<GachaTreasure> maxedOut = List.of(GachaTreasure.values()).stream()
                .filter(treasure -> userTreasureCounts.getOrDefault(treasure, 0) >= MAX_TREASURE_COUNT)
                .toList();
        return GachaTreasure.getRandom(maxedOut);
    }
}
