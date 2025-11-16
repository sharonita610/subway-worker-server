package com.game.subwayworkerserver.util;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;

import java.util.Random;

public class CharacterGenerator {

    private static final Random RANDOM = new Random();

    /**
     * 최소값(min)과 최대값(max) 사이의 랜덤 정수를 계산합니다.
     */
    private static int getRandomStat(int min, int max) {
        if (min > max) {
            return min;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 최소/최대 값을 저장하는 Record.
     */
    public record Range(int min, int max) {}

    /**
     * 생성된 캐릭터의 최종 스탯을 저장하는 Record.
     */
    public record CharacterStats(
            CoworkerElement attribute, //
            int hp,
            int mpNow, // 현재 마나
            int mpMax, // 최대 마나
            int manaRecovery,
            int attack,
            int defense
    ) {
        @Override
        public String toString() {
            // String.format() 출력 필드명도 type으로 변경
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
        double increaseRate = coworkerLevel.getLevelUpStatIncreaseRate();
        double multiplier = 1.0 + increaseRate;

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
        // --- 예시 사용 ---
    public static void main(String[] args) {
        CharacterStats tanker = generateStats(CoworkerType.MOONGSHIL, 1);
        CharacterStats defenseTanker = generateStats(CoworkerType.POOBY, 7);

        System.out.println("--- 랜덤 스탯 생성 결과 ---");
        System.out.println(tanker);
        System.out.println(defenseTanker);
    }
}
