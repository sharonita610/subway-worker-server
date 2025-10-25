package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum PassiveSkillCard {

    // (ID, 표시 이름, 설명, 스킬 타입) - ID는 100번대

    // 101 마음의 평화 (턴 당 마나 회복 증가)
    PEACE_OF_MIND(101, "마음의 평화 (커피 한 잔의 여유)", "턴 당 명상 포인트가 1.1배 증가합니다.", SkillCardType.PASSIVE),

    // 102 날카로운 공격 (상극에게 주는 데미지 증가)
    SHARP_ATTACK(102, "날카로운 공격 (부장님 잔소리)", "상극에게 주는 데미지가 1.1배 증가합니다.", SkillCardType.PASSIVE),

    // 103 따뜻하게 감싸기 (상극에게 받는 데미지 감소)
    WARM_EMBRACE(103, "따뜻하게 감싸기 (과장님의 위로)", "상극에게 받는 데미지가 0.9배로 감소합니다.", SkillCardType.PASSIVE),

    // 104 보호막 (턴마다 방어력 비례 보호막 생성)
    SHIELD(104, "보호막 (잘 만든 보고서)", "매 턴 새로운 보호막이 생성됩니다. 보호막 수치는 내 방어력의 40%입니다.", SkillCardType.PASSIVE),

    // 105 두번째 기회 (HP 20%로 부활)
    SECOND_CHANCE(105, "두번째 기회", "죽으면 HP의 20%로 살아납니다.", SkillCardType.PASSIVE),

    // 106 도플갱어 (동일 캐릭터 대상 공격력 증가)
    DOPPELGANGER(106, "도플갱어", "대전에서 같은 캐릭터를 공격할 때 공격력이 1.2배 증가합니다.", SkillCardType.PASSIVE),

    // 107 보조배터리 (최대 HP 및 턴 당 HP 충전 증가)
    AUX_BATTERY(107, "보조배터리", "최대 HP가 3% 증가하며 턴 당 HP 충전이 1p 증가합니다.", SkillCardType.PASSIVE),

    // 108 양날의 검 (설명 없음 - 기존 플레이스홀더 유지)
    DOUBLE_EDGED_SWORD(108, "양날의 검 (퇴사 시뮬레이션)", "설명 없음 (만들지 고민 중)", SkillCardType.PASSIVE);

    private final int id;
    private final String displayName;
    private final String description; // 새로운 필드
    private final SkillCardType type;

    PassiveSkillCard(int id, String displayName, String description, SkillCardType type) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
    }

    public static String of(int id) {
        for (PassiveSkillCard card : values()) {
            if (card.id == id) {
                return card.displayName;
            }
        }
        throw new BaseException(ErrorCode.NOT_VALID_ID, "유효하지 않은 Passive Skill ID입니다: " + id);
    }
}
