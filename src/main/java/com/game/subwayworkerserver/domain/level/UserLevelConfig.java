package com.game.subwayworkerserver.domain.level;

import com.game.subwayworkerserver.domain.reward.UserLevelReward;
import lombok.Getter;

@Getter
public enum UserLevelConfig {

    LEVEL_RANGE_1_5(1, 5, 200, new UserLevelReward(10000, 1, 0)),

    LEVEL_RANGE_6_10(6, 10, 300, new UserLevelReward(30000, 2, 50)),

    LEVEL_RANGE_11_20(11, 20, 500, new UserLevelReward(50000, 3, 70)),

    LEVEL_RANGE_21_30(21, 30, 700, new UserLevelReward(100000, 5, 100)),

    LEVEL_RANGE_31_40(31, 40, 1000, new UserLevelReward(200000, 5, 300));

    private final int minLevel;
    private final int maxLevel;
    private final int expMultiplier; // 해당 구간의 레벨당 경험치 증가 배수 (레벨 * ExpMultiplier)
    private final UserLevelReward baseReward; // 기본 보상

    UserLevelConfig(int minLevel, int maxLevel, int expMultiplier, UserLevelReward baseReward) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.expMultiplier = expMultiplier;
        this.baseReward = baseReward;
    }

    /**
     * 특정 레벨이 이 구간에 속하는지 확인
     */
    public boolean contains(int level) {
        return level >= minLevel && level <= maxLevel;
    }

    /**
     * 특정 레벨에서 다음 레벨로 가기 위해 필요한 기본 경험치를 계산
     */
    public long getRequiredExpForLevel(int level) {
        // (레벨 * ExpMultiplier) 공식 적용
        return (long) level * this.expMultiplier;
    }

    /**
     * 레벨을 기반으로 해당 구간 설정을 찾는 정적 헬퍼 메서드
     */
    public static UserLevelConfig getConfigByLevel(int level) {
        for (UserLevelConfig config : values()) {
            if (config.contains(level)) {
                return config;
            }
        }
        throw new IllegalArgumentException("정의되지 않은 유저 레벨 구간입니다: " + level);
    }
}