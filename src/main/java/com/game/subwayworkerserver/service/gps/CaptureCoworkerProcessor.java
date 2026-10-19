package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.domain.item.ItemCategory;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 동료 획득에 관련된 확률 판정을 한 곳에서 담당한다.
 * 포획 도구 성공, 트랩 도구 성공, 포획 실패 후 도주의 세 가지 주사위를 굴린다.
 */
@Component
public class CaptureCoworkerProcessor {

    /** 동료 레벨별 기본 포획 확률(%). 도구 확률과 곱해 최종 확률이 된다. */
    private static final Map<Integer, Integer> BASE_CAPTURE_CHANCE_BY_LEVEL = Map.of(
            1, 100,
            2, 80,
            3, 65,
            4, 40,
            5, 20,
            6, 20,
            7, 20
    );

    /** 포획 실패 시 동료 레벨별 도주 확률(%). 표에 없는 레벨은 도주하지 않는다. */
    private static final Map<Integer, Integer> BASE_ESCAPE_CHANCE_BY_LEVEL = Map.of(
            1, 0,
            2, 5,
            3, 5,
            4, 10,
            5, 15,
            6, 15,
            7, 20
    );

    public boolean calculateCaptureSucceed(final String itemConstantName, final int targetLevel) {
        ItemName tool = ItemName.fromConstantName(itemConstantName);
        if (tool.getCategory() != ItemCategory.CAPTURE) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + tool.getName() + "'은 포획(Capture) 도구가 아닙니다.");
        }

        int baseChance = BASE_CAPTURE_CHANCE_BY_LEVEL.getOrDefault(targetLevel, 0);
        if (baseChance <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "정의되지 않았거나 포획 확률이 0%인 레벨입니다: " + targetLevel);
        }
        return roll(baseChance * tool.getPercentage() / 100.0);
    }

    public boolean calculateTrapSucceed(final ItemName tool) {
        if (tool.getCategory() != ItemCategory.TRAP) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + tool.getName() + "'은 트랩(Trap) 도구가 아닙니다.");
        }
        return roll(tool.getPercentage());
    }

    public boolean calculateEscape(final int targetLevel) {
        return roll(BASE_ESCAPE_CHANCE_BY_LEVEL.getOrDefault(targetLevel, 0));
    }

    private static boolean roll(final double chancePercent) {
        return ThreadLocalRandom.current().nextInt(100) < chancePercent;
    }
}
