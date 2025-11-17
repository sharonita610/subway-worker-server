package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum ItemName {

    // (상수명, 한글 이름, 설명, 카테고리, 가격(코인), 포획/효과 비율(%))

    // --- 포획 도구 (Capture Item) ---
    RANDOM_CAPTURE("랜덤 포획", "포획 도구 중 하나를 랜덤으로 획득합니다. (하루 3회 무료)", ItemCategory.CAPTURE, 0, 0),

    // 하급: 2500 코인, 20%,
    PAPER_BOX("종이박스", "동료를 포획할 때 사용할 수 있다. 내구성이 떨어져 동료가 잡혀도 달아날 수 있는 편이다.", ItemCategory.CAPTURE, 2500, 20),

    // 중급: 5000 코인, 50%
    BRIEFCASE("서류가방", "동료를 포획할 때 사용할 수 있다. 지퍼를 여닫을 수 있어 종이박스 보다 포획률이 높다.", ItemCategory.CAPTURE, 5000, 50),

    // 상급: 재화 구매 불가, 90%
    BUSINESS_CARRIER("출장용캐리어", "동료를 포획할 때 사용할 수 있다. 튼튼한 재질로 만들어져 있고 부피가 커서 포획률이 엄청나다.", ItemCategory.CAPTURE, 0, 90), // 가격 0은 재화 구매 불가 의미,


    // --- 트랩 도구 (Trap Item) ---
    RANDOM_TRAP("랜덤 트랩", "트랩 도구 중 하나를 랜덤으로 획득합니다. (하루 3회 무료)", ItemCategory.TRAP, 0, 0),
    // 하급: 4000 코인, 50%

    WOOD_SHOVEL("나무삽", "삽질을하여 적을 구덩이에 빠뜨릴 수 있게한다. 얕게 삽질이 가능하다. (설치 후 12시간 지속)", ItemCategory.TRAP, 4000, 50),
    // 중급: 8000 코인, 70%

    SILVER_SHOVEL("은삽", "삽질을하여 적을 구덩이에 빠뜨릴 수 있게한다. 나무삽 보다는 깊게 삽질이 가능하다. (설치 후 12시간 지속)", ItemCategory.TRAP, 8000, 70),
    // 상급: 재화 구매 불가, 100%

    GOLD_SHOVEL("금삽", "삽질을하여 적을 구덩이에 빠뜨릴 수 있게한다. 삽질 마스터가 가능하다. (설치 후 12시간 지속)", ItemCategory.TRAP, 0, 100);
    // 가격 0은 재화 구매 불가 의미

    // --- 새로 추가할 상수 (가중치 정의) ---
    private static final List<ItemName> CAPTURE_POOL = List.of(
            ItemName.BRIEFCASE,
            ItemName.PAPER_BOX
    );
    private static final List<Integer> CAPTURE_WEIGHTS = List.of(50, 50); // 총합 100

    private static final List<ItemName> TRAP_POOL = List.of(
            ItemName.SILVER_SHOVEL,
            ItemName.WOOD_SHOVEL
    );
    private static final List<Integer> TRAP_WEIGHTS = List.of(50, 50); // 총합 100

    private final String name;
    private final String description;
    private final ItemCategory category;
    private final int price;       // 코인 가격 (재화 구매 불가는 0으로 설정)
    private final int percentage;  // 포획 확률 (%)

    ItemName(String name, String description, ItemCategory category, int price, int percentage) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.percentage = percentage;
    }

    public static ItemName fromKoreanName(final String name) {
        for (ItemName itemName : ItemName.values()) {
            if (itemName.name.equalsIgnoreCase(name)) {
                return itemName;
            }
        }
        throw new BaseException(ErrorCode.BAD_REQUEST, "Unknown ItemName for korean name: " + name);
    }

    private static final Map<Integer, Integer> BASE_CAPTURE_CHANCE_BY_LEVEL = Map.of(
            1, 30, // 레벨 1: 35%
            2, 25, // 레벨 2: 26%
            3, 17, // 레벨 3: 22%
            4, 13, // 레벨 4: 18% (추정)
            5, 8, // 레벨 5: 15% (추정)
            6, 4, // 레벨 6: 12% (추정)
            7, 3 // 레벨 7: 10% (추정)
            // 최대 레벨이 오를 경우 여기에 추가
    );

    public static boolean canCaptureSucceed(final String koreanName, final int level) {
        ItemName tool = fromConstantName(koreanName);

        if (tool.category != ItemCategory.CAPTURE) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + tool.name + "'은 포획(Capture) 도구가 아닙니다.");
        }

        // 1. 레벨별 기본 포획 확률 조회 (baseChance: 레벨에 따른 절대 확률)
        int baseChance = BASE_CAPTURE_CHANCE_BY_LEVEL.getOrDefault(level, 0);

        if (baseChance <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "정의되지 않았거나 포획 확률이 0%인 레벨입니다: " + level);
        }

        // 2. 아이템 확률 가중치 적용
        // tool.percentage는 50, 90 등 100점 만점의 확률값이므로, 100.0으로 나누어 가중치(0.5, 0.9)를 만듭니다.
        double itemMultiplier = tool.percentage / 100.0;

        // 최종 확률 계산: baseChance (30) * multiplier (0.5) = 15.0
        double calculatedChance = baseChance * itemMultiplier;

        // 3. ⭐ 최종 확률 확정 및 최소값 보장 ⭐
        // 반올림하여 정수로 만들고, 최소 1%는 보장하도록 합니다.
        int finalChance = (int) Math.round(calculatedChance);
        finalChance = Math.max(1, finalChance); // 최소 1% 보장 (0% 방지)

        // 4. 포획 시뮬레이션
        int randomRoll = ThreadLocalRandom.current().nextInt(100);

        // 디버깅을 위해 로그를 남기는 것이 좋습니다.
        // log.info("Capture attempt: Level {}, Item Chance {}%, Final Chance {}%, Roll {}", level, tool.percentage, finalChance, randomRoll);

        return randomRoll < finalChance;
    }

    public static ItemName fromConstantName(final String constantName) {
        try {
            return ItemName.valueOf(constantName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Invalid Item Constant Name: " + constantName);
        }
    }

    public static List<ItemName> getTrapList() {
        return Stream.of(ItemName.values())
                .filter(item -> item.category == ItemCategory.TRAP)
                .filter(item -> item != ItemName.GOLD_SHOVEL)
                .collect(Collectors.toList());
    }

    public static List<ItemName> getCaptureList() {
        return Stream.of(ItemName.values())
                .filter(item -> item.category == ItemCategory.CAPTURE)
                .filter(item -> item != ItemName.BUSINESS_CARRIER)
                .collect(Collectors.toList());
    }

    public static ItemName getRandomItem(final String name) {
        ItemName randomTool = fromConstantName(name);

        if (randomTool == ItemName.RANDOM_CAPTURE) {
            return selectRandomItemByWeight(CAPTURE_POOL, CAPTURE_WEIGHTS);
        } else if (randomTool == ItemName.RANDOM_TRAP) {
            return selectRandomItemByWeight(TRAP_POOL, TRAP_WEIGHTS);
        }

        throw new BaseException(ErrorCode.BAD_REQUEST, "유효하지 않은 랜덤 상품 이름입니다: " + name);
    }

    private static ItemName selectRandomItemByWeight(List<ItemName> pool, List<Integer> weights) {
        if (pool.size() != weights.size() || weights.stream().mapToInt(Integer::intValue).sum() != 100) {
            throw new IllegalArgumentException("가중치 풀 또는 가중치 합산 오류가 발생했습니다.");
        }

        // 0부터 99 사이의 랜덤 값 생성 (가중치 총합 100)
        int randomRoll = ThreadLocalRandom.current().nextInt(100);
        int cumulativeWeight = 0;

        for (int i = 0; i < pool.size(); i++) {
            cumulativeWeight += weights.get(i); // 가중치 누적

            if (randomRoll < cumulativeWeight) {
                // 랜덤 값이 현재 누적된 가중치 범위 안에 들어오면 해당 아이템 선택
                return pool.get(i);
            }
        }

        // 안전 장치 (100% 확률 보장 시 여기에 도달하지 않아야 함)
        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "랜덤 아이템 선택 실패.");
    }

    public boolean canTrapSucceed() {
        if (this.category != ItemCategory.TRAP) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + this.name + "'은 트랩(Trap) 도구가 아닙니다. TRAP 카테고리만 호출해야 합니다.");
        }

        if (this.percentage >= 100) {
            return true;
        }

        int randomRoll = ThreadLocalRandom.current().nextInt(100);
        return randomRoll < this.percentage;
    }

}
