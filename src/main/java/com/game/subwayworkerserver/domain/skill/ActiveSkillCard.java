package com.game.subwayworkerserver.domain.skill;

import lombok.Getter;

@Getter
public enum ActiveSkillCard {

    // (ID, 표시 이름, 스킬 타입) - ID는 패시브와 겹치지 않게 200번대로 시작
    RESIGNATION(201, "독뿌리기 (사표 내기)", SkillCardType.ACTIVE),
    FATIGUE_RELIEVER(202, "피로회복제 복용", SkillCardType.ACTIVE),
    BLOOD_DONATION(203, "헌혈", SkillCardType.ACTIVE),
    ARMOR(204, "갑옷", SkillCardType.ACTIVE),
    CPR(205, "CPR", SkillCardType.ACTIVE),
    POISON_GAS(206, "독가스 살포 (부장님 방귀)", SkillCardType.ACTIVE),
    CRITICAL_HIT(207, "회심의 일격", SkillCardType.ACTIVE);

    private final int id;
    private final String displayName;
    private final SkillCardType type; // SkillCardType.ACTIVE로 고정됨

    ActiveSkillCard(int id, String displayName, SkillCardType type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
    }

    /**
     * ID를 사용하여 ActiveSkillCard Enum을 찾아 반환
     */
    public static ActiveSkillCard of(int id) {
        for (ActiveSkillCard card : values()) {
            if (card.id == id) {
                return card;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 Active Skill ID입니다: " + id);
    }
}