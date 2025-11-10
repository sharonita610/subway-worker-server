package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UpgradeExecutor {

    private static final int MAX_LEVEL = 5;
    /**
     * lv 1 count 1 / 2 req  - 1 accumulate
     * lv 1 count 2 / 2 req - 2 accumulate
     * lv 2 count 1 / 3 req - 2 accumulate
     * lv 2 count 2 / 3 req - 3 accumulate
     * lv 2 count 3 / 3 req - 4 accumulate
     * lv 3 count 1 / 4 req - 4 accumulate
     * lv 3 count 2 / 4 req - 5 accumulate
     * lv 3 count 3 / 4 req - 6 accumulate
     * lv 3 count 4 / 4 req - 7 accumulate
     * lv 4 count 1 / 5 req - 7 accumulate
     * lv 4 count 2 / 5 req - 8 accumulate
     * lv 4 count 3 / 5 req - 9 accumulate
     * lv 4 count 4 / 5 req - 10 accumulate
     * lv 4 count 5 / 5 req - 11 accumulate
     * lv 5 count 1 / 6 req - 11 accumulate
     * lv 5 count 2 / 6 req - 12 accumulate
     * lv 5 count 3 / 6 req - 13 accumulate
     * lv 5 count 4 / 6 req - 14 accumulate
     * lv 5 count 5 / 6 req - 15 accumulate
     *
     */
    // ⭐️ [수정된 맵] 해당 레벨 달성 직후, 카운트 1로 시작하기 위해 필요한 총 누적 갯수
    private static final Map<Integer, Integer> LEVEL_START_CUMULATIVE_COUNT = Map.of(
            1, 1,  // Lv 1 시작점 (총 누적 1장 필요)
            2, 3,  // Lv 2 시작점 (총 누적 3장 필요)
            3, 5,  // Lv 3 시작점 (총 누적 5장 필요)
            4, 8,  // Lv 4 시작점 (총 누적 8장 필요)
            5, 12  // Lv 5 시작점 (총 누적 12장 필요)
    );

    /**
     * SkillCard 강화 로직을 실행하여 최종 레벨과 잔여 카운트를 계산합니다.
     */
    public UpgradeResult executeSkillCardLogic(SkillCardEntity targetCard, List<SkillCardEntity> materialCards) {

        // 1. 재료 카드의 총 가치 합산 (Total Accumulated Count는 getAccumulateCount 필드에 있어야 함)
        final int addedMaterialValue = materialCards.stream()
                .mapToInt(SkillCardEntity::getAccumulateCount)
                .sum();

        // 2. ⭐️ 최종 총 누적 획득 카드 수 계산 (레벨업 판정의 유일한 기준)
        int finalTotalAccumulatedCount = targetCard.getAccumulateCount() + addedMaterialValue;

        // 3. 레벨 역산 및 잔여 카운트 계산
        int calculatedLevel = 1;

        // 3-1. 도달 레벨 판정
        for (int level = 1; level < MAX_LEVEL; level++) {
            int requiredToStartNextLevel = LEVEL_START_CUMULATIVE_COUNT.getOrDefault(level, Integer.MAX_VALUE);

            if (finalTotalAccumulatedCount >= requiredToStartNextLevel) {
                // 누적 카운트가 다음 레벨의 시작점보다 크거나 같으면 레벨 증가
                calculatedLevel++;
            } else {
                break;
            }
        }

        // 4. ⭐️ 최종 잔여 카운트 (분자) 계산

        // 해당 레벨이 시작된 시점의 총 누적 갯수를 찾습니다.
        int requiredForCurrentLevelStart = LEVEL_START_CUMULATIVE_COUNT.getOrDefault(calculatedLevel, 1);

        // 최종 잔여 카운트 = (최종 총 누적 갯수) - (최종 레벨 시작에 필요한 누적 갯수) + 1 (Count 1부터 시작)
        int finalRemainingCount = (finalTotalAccumulatedCount - requiredForCurrentLevelStart) + 1;

        if (finalRemainingCount == targetCard.getCurrentLevel() + 1) {
            calculatedLevel++;
            finalRemainingCount = 1;
        } else {
            finalRemainingCount += 1;

        }
        // 5. 최종 결과 반환
        return new UpgradeResult(calculatedLevel, finalRemainingCount, targetCard.getAccumulateCount() + addedMaterialValue);
    }

    public record UpgradeResult(int finalLevel, int finalCount, int accumulateCount) {
    }
}