package com.game.subwayworkerserver.domain.stage;

import java.util.List;
import java.util.Map;

/**
 * 스테이지별 몬스터 정의. 클래스 로딩 시 한 번 만들어지는 불변 데이터라 몇 번을 조회해도 같은 목록을 돌려준다.
 */
public final class StageMonsterData {

    private static final Map<Integer, List<MonsterType>> MONSTERS_BY_STAGE = Map.of(
            1, List.of(
                    new MonsterType(1, "oneEye", "soil", new Hp(50, 50, 0, 0, 0), 20, 2, new Resurrection(true, 0, 0.4)),
                    new MonsterType(2, "orc", "water", new Hp(70, 70, 0, 0, 0), 8, 5, new Resurrection(false, 0)),
                    new MonsterType(3, "oneEye", "fire", new Hp(52, 52, 0, 0, 0), 15, 4, new Resurrection(false, 0))
            ),
            2, List.of(
                    new MonsterType(1, "orc", "soil", new Hp(30, 30, 0, 0, 0), 10, 2, new Resurrection(true, 0, 0.4)),
                    new MonsterType(2, "orc", "water", new Hp(30, 30, 0, 0, 0), 8, 5, new Resurrection(true, 0, 0.3)),
                    new MonsterType(3, "dragon", "fire", new Hp(80, 80, 0, 0, 0), 20, 6, new Resurrection(false, 0)),
                    new MonsterType(4, "orc", "wood", new Hp(28, 28, 0, 0, 0), 5, 6, new Resurrection(true, 0, 0.4)),
                    new MonsterType(5, "orc", "gold", new Hp(20, 20, 3, 0, 0), 4, 9, new Resurrection(true, 0, 0.3))
            )
    );

    private StageMonsterData() {
    }

    /** 스테이지 번호의 몬스터 목록. 정의되지 않은 스테이지면 빈 목록. */
    public static List<MonsterType> forStage(int stageNumber) {
        return MONSTERS_BY_STAGE.getOrDefault(stageNumber, List.of());
    }

    /** 몬스터의 기본 속성 */
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

    /** 몬스터의 체력 관련 속성 */
    public record Hp(
            int nowHp,
            int maxHp,
            int hpRecover,
            int protectSuit,
            int armor
    ) {
    }

    /** 몬스터의 부활 관련 속성. 부활하지 않는 몬스터는 확률이 null. */
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
