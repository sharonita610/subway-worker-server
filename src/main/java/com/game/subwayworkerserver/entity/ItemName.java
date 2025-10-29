package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum ItemName {

    // (상수명, 한글 이름, 설명, 카테고리, 가격(코인), 포획/효과 비율(%))

    // --- 포획 도구 (Capture Item) ---
    // 하급: 2500 코인, 20%
    PAPER_BOX("종이박스", "저렴한 포획 도구. 하루 3개 광고로 구매 가능.", ItemCategory.CAPTURE, 2500, 20),

    // 중급: 5000 코인, 50%
    BRIEFCASE("서류가방", "균형 잡힌 포획 도구.", ItemCategory.CAPTURE, 5000, 50),

    // 상급: 재화 구매 불가, 90%
    BUSINESS_CARRIER("출장용캐리어", "가장 강력한 포획 도구. 보물상자에서만 등장.", ItemCategory.CAPTURE, 0, 90), // 가격 0은 재화 구매 불가 의미

    // --- 트랩 도구 (Trap Item) ---
    // 하급: 4000 코인, 50%
    WOOD_SHOVEL("나무삽", "가장 기본적인 트랩 도구. 50% 확률로 몬스터 포획.", ItemCategory.TRAP, 4000, 50),

    // 중급: 8000 코인, 70%
    SILVER_SHOVEL("은삽", "강력한 트랩 도구. 70% 확률로 몬스터 포획.", ItemCategory.TRAP, 8000, 70),

    // 상급: 재화 구매 불가, 100%
    GOLD_SHOVEL("금삽", "확정 포획이 가능한 트랩 도구. 보물상자에서만 등장.", ItemCategory.TRAP, 0, 100); // 가격 0은 재화 구매 불가 의미

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

    public static boolean canCaptureSucceed(final String koreanName) {
        ItemName tool = fromConstantName(koreanName);

        // 1. 포획 도구 카테고리 확인 (CAPTURE인지 TRAP인지 확인)
        if (tool.category != ItemCategory.CAPTURE) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + tool.name + "'은 포획(Capture) 도구가 아닙니다.");
        }

        // 2. 포획 확률 (%) 추출
        int chance = tool.percentage;

        // 3. 포획 시뮬레이션: 0부터 99 사이의 난수 생성
        int randomRoll = ThreadLocalRandom.current().nextInt(100);
        return randomRoll < chance;
    }

    public static ItemName fromConstantName(final String constantName) {
        try {
            return ItemName.valueOf(constantName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Invalid Item Constant Name: " + constantName);
        }
    }

}
