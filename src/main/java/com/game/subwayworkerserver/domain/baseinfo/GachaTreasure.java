package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.*;

@Getter
public enum GachaTreasure {

    ATTACK_UP("결재 도장", "공격력 상승"),

    DEFENSE_UP("헤드셋", "방어력 상승"),

    COIN_GAIN_UP("보너스 봉투", "코인 획득 상승"),

    HP_INCREASE("베개", "HP 증가"),

    MP_RECOVERY_UP("에너지 드링크", "MP 회복량 증가"),

    CAPTURE_SUCCESS_RATE_UP("레이저 포인터", "포획 성공 확률 증가"),

    TRAP_SUCCESS_RATE_UP("접착 메모지", "덫 성공 확률 증가");

    private final String itemName;
    private final String description;

    GachaTreasure(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    public static GachaTreasure fromItemName(final String itemName) {
        for (GachaTreasure treasure : GachaTreasure.values()) {
            if (treasure.name().equalsIgnoreCase(itemName)) {
                return treasure;
            }
        }
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 아이템 이름입니다: " + itemName);
    }

    public static GachaTreasure getRandom(List<GachaTreasure> treasuresToExclude) {
        GachaTreasure[] allTreasures = GachaTreasure.values();
        List<GachaTreasure> availableTreasures = Arrays.stream(allTreasures)
                .filter(treasure -> !treasuresToExclude.contains(treasure))
                .toList();

        if (availableTreasures.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "획득 가능한 보물 아이템이 남아있지 않습니다.");
        }

        int count = availableTreasures.size();
        int randomIndex = ThreadLocalRandom.current().nextInt(count);

        return availableTreasures.get(randomIndex);
    }

    public static boolean isGacha(final String type) {
        return stream(GachaTreasure.values())
                .anyMatch(treasure -> treasure.name().equalsIgnoreCase(type));
    }
}