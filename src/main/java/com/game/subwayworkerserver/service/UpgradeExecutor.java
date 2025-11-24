package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
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
            1, 1,
            2, 2,
            3, 4,
            4, 7,
            5, 11
    );

    /**
     * SkillCard 강화 로직을 실행하여 최종 레벨과 잔여 카운트를 계산합니다.
     */
    public UpgradeResult executeSkillCardLogic(SkillCardEntity targetCard, List<SkillCardEntity> materialCards) {

        // 1. 재료 카드의 총 가치 합산 (Total Accumulated Count는 getAccumulateCount 필드에 있어야 함)
        final int addedMaterialValue = materialCards.stream()
                .mapToInt(SkillCardEntity::getAccumulateCount)
                .sum();

        int finalTotalAccumulatedCount = targetCard.getAccumulateCount() + addedMaterialValue;

        int calculatedLevel = 1;
        int requiredForCurrentLevelStart = 1;

        // 2. 레벨 역순 순회 및 판정 (수정된 맵 사용)
        List<Integer> sortedLevels = LEVEL_START_CUMULATIVE_COUNT.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        for (int level : sortedLevels) {
            int requiredStartCount = LEVEL_START_CUMULATIVE_COUNT.get(level);

            if (finalTotalAccumulatedCount >= requiredStartCount) {
                calculatedLevel = level;
                requiredForCurrentLevelStart = requiredStartCount;
                break;
            }
        }

        // 3. 현재 레벨의 최대 필요 Count (Req) 계산
        int requiredToStartNextLevel = LEVEL_START_CUMULATIVE_COUNT.getOrDefault(calculatedLevel + 1, Integer.MAX_VALUE);
        int maxRequiredCountForCurrentLevel = requiredToStartNextLevel - requiredForCurrentLevelStart;

        // 4. 최종 잔여 카운트 (Count 1부터 시작)
        // 예시: 누적 2 (목표) - 시작 2 = 0. Count는 1부터 시작하므로 +1 필요
        int finalRemainingCount = (finalTotalAccumulatedCount - requiredForCurrentLevelStart) + 1;

        // 5. ⭐ 레벨업 조건 확인 (Max Count 초과 시 레벨업) ⭐
        if (finalRemainingCount > maxRequiredCountForCurrentLevel) {
            calculatedLevel++;
            finalRemainingCount = 1; // 다음 레벨의 1/N로 리셋
        }

        return new UpgradeResult(calculatedLevel, finalRemainingCount, finalTotalAccumulatedCount, addedMaterialValue);
    }

    public record UpgradeResult(int finalLevel, int finalCount, int accumulateCount, int addedMaterialValue) {
    }
}