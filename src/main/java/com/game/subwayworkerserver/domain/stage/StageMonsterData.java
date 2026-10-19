package com.game.subwayworkerserver.domain.stage;


import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

public class StageMonsterData {

    // 몬스터 1 데이터 리스트
    public static List<MonsterType> sampleMonster1 = new ArrayList<>();

    // 몬스터 2 데이터 리스트
    public static List<MonsterType> sampleMonster2 = new ArrayList<>();

    /**
     * 모든 샘플 데이터를 초기화하는 메서드
     */
    @PostConstruct
    public static void initializeData() {
        // --- sampleMonster1 초기화 ---

        // 몬스터 1-1: oneEye, soil
        Hp hp1_1 = new Hp(50, 50, 0, 0, 0);
        Resurrection res1_1 = new Resurrection(true, 0, 0.4);
        sampleMonster1.add(new MonsterType(1, "oneEye", "soil", hp1_1, 20, 2, res1_1));

        // 몬스터 1-2: orc, water
        Hp hp1_2 = new Hp(70, 70, 0, 0, 0);
        Resurrection res1_2 = new Resurrection(false, 0);
        sampleMonster1.add(new MonsterType(2, "orc", "water", hp1_2, 8, 5, res1_2));

        // 몬스터 1-3: oneEye, fire
        Hp hp1_3 = new Hp(52, 52, 0, 0, 0);
        Resurrection res1_3 = new Resurrection(false, 0);
        sampleMonster1.add(new MonsterType(3, "oneEye", "fire", hp1_3, 15, 4, res1_3));

        // --- sampleMonster2 초기화 ---

        // 몬스터 2-1: orc, soil
        Hp hp2_1 = new Hp(30, 30, 0, 0, 0);
        Resurrection res2_1 = new Resurrection(true, 0, 0.4);
        sampleMonster2.add(new MonsterType(1, "orc", "soil", hp2_1, 10, 2, res2_1));

        // 몬스터 2-2: orc, water
        Hp hp2_2 = new Hp(30, 30, 0, 0, 0);
        Resurrection res2_2 = new Resurrection(true, 0, 0.3);
        sampleMonster2.add(new MonsterType(2, "orc", "water", hp2_2, 8, 5, res2_2));

        // 몬스터 2-3: dragon, fire
        Hp hp2_3 = new Hp(80, 80, 0, 0, 0);
        Resurrection res2_3 = new Resurrection(false, 0);
        sampleMonster2.add(new MonsterType(3, "dragon", "fire", hp2_3, 20, 6, res2_3));

        // 몬스터 2-4: orc, wood
        Hp hp2_4 = new Hp(28, 28, 0, 0, 0);
        Resurrection res2_4 = new Resurrection(true, 0, 0.4);
        sampleMonster2.add(new MonsterType(4, "orc", "wood", hp2_4, 5, 6, res2_4));

        // 몬스터 2-5: orc, gold
        Hp hp2_5 = new Hp(20, 20, 3, 0, 0);
        Resurrection res2_5 = new Resurrection(true, 0, 0.3);
        sampleMonster2.add(new MonsterType(5, "orc", "gold", hp2_5, 4, 9, res2_5));
    }

    // ----------------------------------------------------
    // --- 데이터 모델 클래스 (Static Nested Classes) ---
    // ----------------------------------------------------

     /**
     * 몬스터의 기본 속성을 정의하는 클래스
     */
     public record MonsterType(
                int id,
                String type,
                String element,
                Hp hp,
                int attack,
                int defense,
                Resurrection resurrection
    ) {

    }

    /**
     * 몬스터의 체력 관련 속성을 정의하는 클래스
     *
     * @param nowHp 게터 (필수)
     */
    public record Hp(
            int nowHp,
            int maxHp,
            int hpRecover,
            int protectSuit,
            int armor
    ) {

    }

    /**
     * 몬스터의 부활 관련 속성을 정의하는 클래스
     *
     * @param hasResurrection 게터 (필수)
     */
    public record Resurrection(
            boolean hasResurrection,
            int resurrectionTurn,
            Double resurrectionRate
    ) {

        public Resurrection(boolean hasResurrection, int resurrectionTurn) {
                this(hasResurrection, resurrectionTurn, null);
            }

        }
}