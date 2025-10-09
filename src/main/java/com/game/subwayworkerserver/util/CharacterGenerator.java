package com.game.subwayworkerserver.util;

import com.game.subwayworkerserver.domain.baseinfo.FriendType;

import java.util.Random;

import static com.game.subwayworkerserver.domain.baseinfo.FriendType.BALANCED;

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
            String type, // 직업명/타입 (예: "탱커")
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
                    type, hp, mpNow, mpMax, manaRecovery, attack, defense
            );
        }
    }

    /**
     * 특정 FriendType(스탯 프로필)을 기반으로 랜덤 스탯을 생성합니다.
     *
     * @param type 스탯 계산에 사용할 FriendType Enum
     * @return 랜덤 스탯이 부여된 CharacterStats 객체
     */
    public static CharacterStats generateStats(FriendType type) {

        // 1. HP: 단일 값으로 계산
        int hpValue = getRandomStat(type.getHpRange().min(), type.getHpRange().max());

        // 2. MP Max: 최대 마나 계산
        int maxMp = getRandomStat(type.getMpMaxRange().min(), type.getMpMaxRange().max());

        // 3. MP Now: 초기 마나 계산. 최대 마나를 초과하지 않도록 보장합니다.
        int initialMp = Math.min(
                getRandomStat(type.getMpNowRange().min(), type.getMpNowRange().max()),
                maxMp
        );

        // 4. 공격력 / 방어력
        int attack = getRandomStat(type.getAttackRange().min(), type.getAttackRange().max());
        int defense = getRandomStat(type.getDefenseRange().min(), type.getDefenseRange().max());

        // FriendType의 getJob() 대신 getJob()의 역할을 하던 필드를 type으로 사용
        return new CharacterStats(
                type.getType(), // FriendType의 getJob()을 그대로 사용하여 직업명을 가져옵니다.
                hpValue,
                initialMp, // 현재 MP
                maxMp,     // 최대 MP
                type.getManaRecovery(),
                attack,
                defense
        );
    }

    public static CharacterStats generateDefaultFriendStats() {
        return new CharacterStats(
                BALANCED.getType(),
                53,
                11,
                47,
                BALANCED.getManaRecovery(),
                19,
                8
        );

    }

    public static CharacterGenerator.CharacterStats getRandomFriendStat() {
        FriendType[] types = FriendType.values();
        int randomIndex = RANDOM.nextInt(types.length);
        FriendType type = types[randomIndex];
        return generateStats(type);
    }
        // --- 예시 사용 ---
    public static void main(String[] args) {
        CharacterStats tanker = generateStats(FriendType.TANKER);
        CharacterStats defenseTanker = generateStats(FriendType.DEFENSE_TANKER);

        System.out.println("--- 랜덤 스탯 생성 결과 ---");
        System.out.println(tanker);
        System.out.println(defenseTanker);
    }
}
