package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum PassiveSkillCard {


    PEACE_OF_MIND(101, "커피 한 잔의 여유", "턴 당 명상 포인트가 1.1배 증가합니다.",
            SkillCardType.PASSIVE, 1.1, 0.10, "시작 시점."), // 1.1배 -> 1.1, 10p 증가 -> 0.10

    SHARP_ATTACK(102, "부장님 잔소리", "상극에게 주는 데미지가 1.1배 증가합니다.",
            SkillCardType.PASSIVE, 1.1, 0.10, "내 턴"),

    WARM_EMBRACE(103, "동기의 위로", "상극에게 받는 데미지가 0.9배로 감소합니다.",
            SkillCardType.PASSIVE, 0.9, -0.10, "상대 턴 공격"), // 0.9배 -> 0.9, 10p 감소 -> -0.10 (감소율)

    SHIELD(104, "잘 만든 보고서", "매 턴 새로운 보호막이 생성됩니다. 보호막 수치는 내 방어력의 40%입니다.",
            SkillCardType.PASSIVE, 0.40, 0.05, "시작 시점."), // 40% -> 0.40, 5p 증가 -> 0.05

    SECOND_CHANCE(105, "두번째 기회", "죽으면 HP의 20%로 살아납니다. 일정 HP로 부활.",
            SkillCardType.PASSIVE, 0.20, 0.05, "상대 턴 공격 + 내가 죽을 때 resurrection"), // 20% -> 0.20, 5p 증가 -> 0.05

    DOPPELGANGER(106, "도플갱어", "대전에서 같은 캐릭터를 공격할 때 공격력이 1.2배 증가합니다.",
            SkillCardType.PASSIVE, 1.2, 0.10, "내 턴 공격"),

    AUX_BATTERY(107, "보조배터리", "최대 HP가 3% 증가하며 턴 당 HP 충전이 1p 증가합니다.",
            SkillCardType.PASSIVE, 0.03, 0.01, "시작 시점."); // 3% HP 증가 -> 0.03, 1p 증가 -> 0.01 (두 가지 효과 중 하나만 baseEffectValue에 매핑)


    private final int id;
    private final String koreanName;
    private final String description;
    private final SkillCardType type;

    private final double baseEffectValue; // 1레벨 효과 수치 (예: 1.1배, 0.40%)
    private final double levelUpIncrease; // 레벨당 증가량 (예: 0.10)
    private final String timing;          // 계산 시간

    PassiveSkillCard(int id, String koreanName, String description, SkillCardType type,
                     double baseEffectValue, double levelUpIncrease, String timing) {
        this.id = id;
        this.koreanName = koreanName;
        this.description = description;
        this.type = type;
        this.baseEffectValue = baseEffectValue;
        this.levelUpIncrease = levelUpIncrease;
        this.timing = timing;
    }

    public static PassiveSkillCard from(int id) {
        for (PassiveSkillCard card : values()) {
            if (card.id == id) {
                return card;
            }
        }
        throw new BaseException(ErrorCode.NOT_VALID_ID, "유효하지 않은 Passive Skill ID입니다: " + id);
    }

    /**
     * 특정 레벨에서의 최종 효과 수치를 계산합니다.
     *
     * @param skillLevel 현재 스킬 레벨 (1부터 시작)
     * @return 계산된 최종 효과 수치
     */
    public double getFinalEffectValue(int skillLevel) {
        if (skillLevel <= 1) {
            return this.baseEffectValue;
        }

        // 최종 효과 = 기본 값 + (레벨 - 1) * 레벨당 증가량
        return this.baseEffectValue + (skillLevel - 1) * this.levelUpIncrease;
    }
}