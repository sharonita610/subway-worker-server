package com.game.subwayworkerserver.domain.skill;

import lombok.Getter;

@Getter
public enum PassiveSkillCard {

    PEACE_OF_MIND(101, "마음의 평화 (커피 한 잔의 여유)", SkillCardType.PASSIVE),
    SHARP_ATTACK(102, "날카로운 공격 (부장님 잔소리)", SkillCardType.PASSIVE),
    WARM_EMBRACE(103, "따뜻하게 감싸기 (과장님의 위로)", SkillCardType.PASSIVE),
    ARMOR_OF_REPORT(104, "보호막 (잘 만든 보고서)", SkillCardType.PASSIVE),
    SECOND_CHANCE(105, "두번째 기회", SkillCardType.PASSIVE),
    DOPPELGANGER(106, "도플갱어", SkillCardType.PASSIVE),
    DOUBLE_EDGED_SWORD(107, "양날의 검 (만들지 고민 중) (퇴사 시뮬레이션)", SkillCardType.PASSIVE),
    AUX_BATTERY(108, "보조배터리", SkillCardType.PASSIVE);

    private final int id;
    private final String displayName;
    private final SkillCardType type;

    PassiveSkillCard(int id, String displayName, SkillCardType type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
    }

    public static PassiveSkillCard of(int id) {
        for (PassiveSkillCard card : values()) {
            if (card.id == id) {
                return card;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 Passive Skill ID입니다: " + id);
    }
}