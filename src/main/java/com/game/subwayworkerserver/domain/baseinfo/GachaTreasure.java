package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

@Getter
public enum GachaTreasure {

    // 1. 공격력 상승 효과
    ATTACK_UP("결재 도장", "공격력 상승"),

    // 2. 방어력 상승 효과
    DEFENSE_UP("헤드셋", "방어력 상승"),

    // 3. 코인 획득 상승 효과
    COIN_GAIN_UP("보너스 봉투", "코인 획득 상승"),

    // 4. HP 증가 효과
    HP_INCREASE("베개", "HP 증가"),

    // 5. MP 회복량 증가 효과
    MP_RECOVERY_UP("에너지 드링크", "MP 회복량 증가"),

    // 6. 포획 성공 확률 증가 효과 (CAPTURE 도구 보조)
    CAPTURE_SUCCESS_RATE_UP("레이저 포인터", "포획 성공 확률 증가"),

    // 7. 덫 성공 확률 증가 효과 (TRAP 도구 보조)
    TRAP_SUCCESS_RATE_UP("접착 메모지", "덫 성공 확률 증가");


    private final String itemName;
    private final String description;

    GachaTreasure(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    public static GachaTreasure fromItemName(final String itemName) {
        for (GachaTreasure effect : GachaTreasure.values()) {
            if (effect.itemName.equalsIgnoreCase(itemName)) {
                return effect;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 아이템 이름입니다: " + itemName);
    }
}