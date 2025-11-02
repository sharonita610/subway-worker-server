package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GachaRandomGiftGenerator {

    // --- 기본 재화 상수 (Basic Rewards) ---
    private static final long COIN_MIN = 10000;
    private static final long COIN_MAX = 200000;
    private static final int TICKET_MIN = 3;
    private static final int TICKET_MAX = 200;
    private static final double SKEW_FACTOR = 3.5; // 낮은 값에 치우친 분포를 위한 인자

    // --- 추가 보상 확률 설정 (Extra Rewards) ---
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

    // ----------------------------------------------------
    // --- 1. 기본 재화 계산 로직 ---
    // ----------------------------------------------------

    public static List<DefaultGift> getDefaultReward() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 코인 (낮은 값에 치우친 분포 적용)
        long coinRange = COIN_MAX - COIN_MIN;
        double skewedRandomCoin = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        long coins = (long) (COIN_MIN + skewedRandomCoin * coinRange);

        // 휴가티켓 (낮은 값에 치우친 분포 적용)
        int ticketRange = TICKET_MAX - TICKET_MIN;
        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN + skewedRandomTicket * ticketRange);

        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));

        return defaultGiftList;
    }

    // ----------------------------------------------------
    // --- 2. 추가 보상 계산 로직 (Extra Gift) ---
    // ----------------------------------------------------

    /**
     * 추가 제공 보상을 계산하고, 획득한 보상 목록을 반환합니다. (모두 if문 방식)
     * 보물 획득 시, GachaTreasure Enum을 사용하여 최종 보물 아이템을 결정합니다.
     *
     * @return 획득에 성공한 ExtraGift 목록
     */
    public static List<ExtraGift> getExtraReward() {
        List<ExtraGift> rewards = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (RewardConfig config : EXTRA_REWARDS_CONFIGS) {
            int requiredProbability = config.chancePercent();

            // 0.0부터 100.0 사이의 난수 생성
            double roll = random.nextDouble(0, 100);

            // 난수 값이 요구 확률보다 낮으면 획득 성공
            if (roll < requiredProbability) {

                // '보물'일 경우: GachaTreasure 랜덤 계산 로직 호출
                if (config.type().equals("TREASURE")) {
                    // 보물 1개 획득 시, 다시 어떤 보물을 줄지 계산 (3개 랜덤 선택)
                    List<GachaTreasure> treasures = GachaTreasure.getRandom();

                    // 획득한 모든 보물 아이템을 rewards 리스트에 추가 (개별 아이템명으로)
                    treasures.stream()
                            .map(treasure -> new ExtraGift(treasure.getItemName(), 1)) // 아이템 이름으로 변환
                            .collect(Collectors.toCollection(() -> rewards));

                } else {
                    // 나머지 아이템은 1개씩 지급 (중급 채집툴, 공동 동료 등)
                    rewards.add(new ExtraGift(config.type(), 1));
                }
            }
        }

        return rewards;
    }

    // 기본 보상 DTO (코인, 휴가티켓, 경험치)
    public record DefaultGift(String type, String displayName, int quantity) {
    }

    // 추가 보상 DTO (아이템, 보물 등)
    public record ExtraGift(String type, int quantity) {
    }
}