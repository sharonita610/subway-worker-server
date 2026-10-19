package com.game.subwayworkerserver.util;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;

import java.util.Random;

public class CharacterGenerator {

    private static final Random RANDOM = new Random();

    private static int getRandomStat(int min, int max) {
        if (min > max) {
            return min;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public record Range(int min, int max) {}

    public record CharacterStats(
            CoworkerElement attribute,
            int hp,
            int mpNow,
            int mpMax,
            int manaRecovery,
            int attack,
            int defense
    ) {
        @Override
        public String toString() {
            return String.format(
                    "[직업: %s] HP: %d, MP: %d/%d (회복:%d), 공격: %d, 방어: %d",
                    attribute, hp, mpNow, mpMax, manaRecovery, attack, defense
            );
        }
    }

    /**
     * 특정 CoworkerType(스탯 프로필)을 기반으로 랜덤 스탯을 생성합니다.
     *
     * @param type 스탯 계산에 사용할 CoworkerType Enum
     * @return 랜덤 스탯이 부여된 CharacterStats 객체
     */
    public static CharacterStats generateStats(CoworkerType type, int level) {

        int hpValue = getRandomStat(type.getHpRange().min(), type.getHpRange().max());
        int maxMp = getRandomStat(type.getMpMaxRange().min(), type.getMpMaxRange().max());
        int initialMp = Math.min(
                getRandomStat(type.getMpNowRange().min(), type.getMpNowRange().max()),
                maxMp
        );
        int attack = getRandomStat(type.getAttackRange().min(), type.getAttackRange().max());
        int defense = getRandomStat(type.getDefenseRange().min(), type.getDefenseRange().max());
        CoworkerLevel coworkerLevel = CoworkerLevel.of(level);
        double multiplier = coworkerLevel.getStatRatioToInitial();

        return new CharacterStats(
                CoworkerElement.getRandomAttribute(),
                (int) Math.round(hpValue * multiplier),
                (int) Math.round(initialMp * multiplier),
                (int) Math.round(maxMp * multiplier),
                (int) Math.round(type.getManaRecovery() * multiplier),
                (int) Math.round(attack * multiplier),
                (int) Math.round(defense * multiplier)
        );
    }

    public static CharacterStats generateDefaultCoworkerStats(final CoworkerType type) {
        return new CharacterStats(
                CoworkerElement.getRandomAttribute(),
                type.getHpRange().min(),
                type.getMpNowRange().min(),
                type.getMpMaxRange().min(),
                type.getManaRecovery(),
                type.getAttackRange().min(),
                type.getDefenseRange().min()
        );
    }

    public static CharacterGenerator.CharacterStats getRandomCoworkerStat(CoworkerType coworkerType, int level) {
        return generateStats(coworkerType, level);
    }
}
