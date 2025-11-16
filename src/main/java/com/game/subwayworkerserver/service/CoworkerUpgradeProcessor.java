package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.service.dto.CoworkerUpgradeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class CoworkerUpgradeProcessor {

    private static final String ENERZ = "ENERZ";
    public static final double EXTRA_MULTIPLIER = 1.2;

    private static final int BASE_EXTRA_EXP = 50;

    public CoworkerUpgradeVO upgradeCoworker(final CoworkerEntity target, final List<CoworkerEntity> materials) {
        CoworkerType tagetType = target.getCoworkerType();

        int materialTotalExp = getMaterialTotalExp(materials, tagetType);
        double baseExpPoint = materialTotalExp * 0.4;
        int expToUpgrade = getUpgradeFinalPoint(baseExpPoint);

        return CoworkerUpgradeVO.of(baseExpPoint, expToUpgrade);
    }

    private static int getMaterialTotalExp(List<CoworkerEntity> materials, CoworkerType tagetType) {
        return materials.stream()
                .mapToInt(material -> {
                    CoworkerType coworkerType = material.getCoworkerType();
                    double finalExp;

                    if (coworkerType.name().equals(ENERZ) || coworkerType.equals(tagetType)) {
                        finalExp = (material.getTotalExp() + BASE_EXTRA_EXP) * EXTRA_MULTIPLIER;
                    } else {
                        finalExp = material.getTotalExp() + BASE_EXTRA_EXP;
                    }

                    return (int) Math.round(finalExp);
                })
                .sum();
    }

    private int getUpgradeFinalPoint(double basePoint) {
        int probabilityRoll = ThreadLocalRandom.current().nextInt(1, 101);
        double finalRate;

        if (probabilityRoll <= 5) {
            finalRate = 0.70; // 5% 이하 (70%)
        } else if (probabilityRoll <= 15) { // 5% 초과 ~ 15% 이하
            finalRate = 0.80; // (80%)
        } else if (probabilityRoll <= 30) { // 15% 초과 ~ 30% 이하
            finalRate = 0.90; // (90%)
        } else if (probabilityRoll <= 60) { // 30% 초과 ~ 60% 이하
            finalRate = 1.00; // (100%)
        } else if (probabilityRoll <= 80) { // 60% 초과 ~ 80% 이하
            finalRate = 1.20; // (120%)
        } else if (probabilityRoll <= 90) { // 80% 초과 ~ 90% 이하
            finalRate = 1.30; // (130%)
        } else { // 90% 초과 ~ 100% 이하 (기존 150%에 해당하는 10% 확률을 140%로 설정)
            finalRate = 1.40; // (140%)
        }
        return (int) (basePoint * finalRate);
    }
}
