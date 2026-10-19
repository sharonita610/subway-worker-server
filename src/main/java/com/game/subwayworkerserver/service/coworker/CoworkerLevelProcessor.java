package com.game.subwayworkerserver.service.coworker;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.springframework.stereotype.Component;

@Component
public class CoworkerLevelProcessor {

    private static final int MAX_LEVEL = 7;
    public void processCoworkerExpAndLevelUp(CoworkerEntity coworker, int exp) {
        coworker.updateExp(exp);
        checkAndProcessLevelUp(coworker);
    }

    private void checkAndProcessLevelUp(CoworkerEntity coworker) {
        int currentLevel = coworker.getLevel().getLevel();
        int currentExp = coworker.getCurrentExp();

        while (currentLevel < MAX_LEVEL) {
            CoworkerLevel levelInfo = CoworkerLevel.of(currentLevel);
            long requiredExp = levelInfo.getRequiredExpToNextLevel();

            if (currentExp >= requiredExp) {
                currentExp -= (int) requiredExp;

                currentLevel++;

                CoworkerLevel newLevelInfo = CoworkerLevel.of(currentLevel);
                coworker.levelUp(newLevelInfo, currentExp);

            } else {

                break;
            }
        }
    }
}
