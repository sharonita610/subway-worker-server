package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpgradeExecutor {

    private static final int MAX_LEVEL = 5;
    private static final Map<Integer, Integer> LEVEL_UP_REQUIREMENTS = Map.of(
            1, 1, // Lv1 -> Lv2에 필요한 추가 카드 수: 1장
            2, 2, // Lv2 -> Lv3에 필요한 추가 카드 수: 2장
            3, 3, // Lv3 -> Lv4에 필요한 추가 카드 수: 3장
            4, 4  // Lv4 -> Lv5에 필요한 추가 카드 수: 4장
    );


    public UpgradeResult executeSkillCardLogic(SkillCardEntity targetCard, int addCardCount) {

        int currentLevel = targetCard.getCurrentLevel();
        int totalNewCount = targetCard.getCount() + addCardCount;
        while (currentLevel < MAX_LEVEL) {
            int requiredCards = LEVEL_UP_REQUIREMENTS.getOrDefault(currentLevel, MAX_LEVEL + 1);
            if (totalNewCount >= requiredCards) {
                totalNewCount -= requiredCards;
                currentLevel++;
                if (currentLevel >= MAX_LEVEL) {
                    break;
                }
            } else {
                break;
            }
        }
        return new UpgradeResult(currentLevel, totalNewCount);
    }

    public record UpgradeResult(int finalLevel, int finalCount) {
    }

}
