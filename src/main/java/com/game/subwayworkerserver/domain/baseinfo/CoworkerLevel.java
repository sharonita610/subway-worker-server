package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum CoworkerLevel {

    INTERN(1, "인턴", 200, 200, 30000, 0.0, 1.0),
    WORKER(2, "사원", 300, 500, 50000, 0.05, 1.06), // level 2: 이전 * 105% (0.05 증가)
    SENIOR(3, "주임", 500, 1000, 70000, 0.10, 1.155), // level 3: 이전 * 110% (0.10 증가) -> 스크린샷 110%
    ASSISTANT_MANAGER(4, "대리", 700, 1700, 100000, 0.15, 1.328), // level 4: 이전 * 115% (0.15 증가)
    MANAGER(5, "과장", 1000, 2700, 200000, 0.20, 1.594), // level 5: 이전 * 120% (0.20 증가)
    TEAM_LEADER(6, "팀장", 1500, 4200, 300000, 0.30, 2.072), // level 6: 이전 * 130% (0.30 증가)
    CHIEF(7, "차장", 0, 4200, 0, 0.50, 3.108); // level 7: 현재 만렙, 다음 레벨 필요 경험치 0

    private final int level;
    private final String title;
    private final int requiredExpToNextLevel; // 다음 레벨로 가기 위해 필요한 경험치 (현재 레벨 0부터 시작)l
    private final int cumulativeExp;          // 1레벨부터 누적 경험치
    private final int upgradeCost;            // 강화를 통한 업그레이드 비용
    private final double levelUpStatIncreaseRate; // 레벨업 시 수치 증가 (이전 대비 증가율) (예: 105% -> 0.05)
    private final double statRatioToInitial;  // 초기 능력치 대비 비율

    CoworkerLevel(
            int level,
            String title,
            int requiredExpToNextLevel,
            int cumulativeExp,
            int upgradeCost,
            double levelUpStatIncreaseRate,
            double statRatioToInitial
    ) {
        this.level = level;
        this.title = title;
        this.requiredExpToNextLevel = requiredExpToNextLevel;
        this.cumulativeExp = cumulativeExp;
        this.upgradeCost = upgradeCost;
        this.levelUpStatIncreaseRate = levelUpStatIncreaseRate;
        this.statRatioToInitial = statRatioToInitial;
    }

    public static CoworkerLevel of(int level) {
        for (CoworkerLevel sl : values()) {
            if (sl.level == level) {
                return sl;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 SkillLevel 번호입니다: " + level);
    }

    public static CoworkerLevel getRandomLevel() {
        CoworkerLevel[] levels = CoworkerLevel.values();

        double totalWeight = 0;
        for (int i = 0; i < levels.length; i++) {
            totalWeight += 1.0 / levels[i].level;
        }
        double randomValue = ThreadLocalRandom.current().nextDouble(0, totalWeight);

        double cumulativeWeight = 0;
        for (CoworkerLevel level : levels) {
            cumulativeWeight += 1.0 / level.level;

            if (randomValue <= cumulativeWeight) {
                return level;
            }
        }
        return levels[0];
    }

    public static CoworkerLevel getSpecialCoworkerLevel() {
        List<CoworkerLevel> targetLevels = Arrays.stream(CoworkerLevel.values())
                .filter(level -> level.level >= 1 && level.level <= 4)
                .toList();

        if (targetLevels.isEmpty()) {
            return INTERN;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(targetLevels.size());
        return targetLevels.get(randomIndex);
    }

    public int getBaseXpForMaterial() {
        return switch (this) {
            case INTERN -> 200;
            case WORKER -> 300;
            case SENIOR -> 500;
            case ASSISTANT_MANAGER -> 700;
            case MANAGER -> 1000;
            case TEAM_LEADER -> 1500;
            case CHIEF -> 0;
        };
    }
}
