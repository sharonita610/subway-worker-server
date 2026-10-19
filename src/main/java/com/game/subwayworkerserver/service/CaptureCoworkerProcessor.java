package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.coworker.CaptureResponse;
import com.game.subwayworkerserver.domain.item.ItemCategory;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class CaptureCoworkerProcessor {

    private static final Map<Integer, Integer> BASE_CAPTURE_CHANCE_BY_LEVEL = Map.of(
            1, 100,
            2, 80,
            3, 65,
            4, 40,
            5, 20,
            6, 20,
            7, 20
    );

    private static final Map<Integer, Integer> BASE_ESCAPE_CHANCE_BY_LEVEL = Map.of(
            1, 0,
            2, 5,
            3, 5,
            4, 10,
            5, 15,
            6, 15,
            7, 20
    );

    public boolean calculateCaptureSucceed(final String itemKoreanName, final int targetLevel) {

        ItemName tool = ItemName.fromConstantName(itemKoreanName);

        if (tool.getCategory() != ItemCategory.CAPTURE) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "'" + tool.getName() + "'은 포획(Capture) 도구가 아닙니다.");
        }

        int baseChance = BASE_CAPTURE_CHANCE_BY_LEVEL.getOrDefault(targetLevel, 0);

        if (baseChance <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "정의되지 않았거나 포획 확률이 0%인 레벨입니다: " + targetLevel);
        }

        double calculatedChance = baseChance * tool.getPercentage() / 100.0;
        int randomRoll = ThreadLocalRandom.current().nextInt(100);
        return randomRoll < calculatedChance;

    }

    public CaptureResponse calculateEscape(int targetLevel) {
        final int ESCAPE_PROBABILITY = BASE_ESCAPE_CHANCE_BY_LEVEL.getOrDefault(targetLevel, 0);
        int randomRoll = ThreadLocalRandom.current().nextInt(100);

        if (randomRoll < ESCAPE_PROBABILITY) {
            return CaptureResponse.of(false, true);

        } else {
            return CaptureResponse.of(false, false);
        }
    }
}
