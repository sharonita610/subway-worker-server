package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.CaptureResponse;
import com.game.subwayworkerserver.entity.ItemCategory;
import com.game.subwayworkerserver.entity.ItemName;
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
            6, 5,
            7, 1
    );

    private static final Map<Integer, Integer> BASE_ESCAPE_CHANCE_BY_LEVEL = Map.of(
            1, 10,
            2, 15,
            3, 20,
            4, 30,
            5, 40,
            6, 40,
            7, 40
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
