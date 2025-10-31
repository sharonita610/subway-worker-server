package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum ActiveSkillCard {


    // 202 피로회복제 복용 (내 HP 회복)
    FATIGUE_RELIEVER(202, "피로회복제 복용", "내 HP의 20%를 회복합니다. (필요 마나: 30)", SkillCardType.ACTIVE),

    // 203 헌혈 (내 HP 깎고 아군 회복)
    BLOOD_DONATION(203, "헌혈", "내 HP를 깎고, 선택한 캐릭터의 HP를 20% 회복합니다. (필요 마나: 35)", SkillCardType.ACTIVE),

    // 204 명예수방관 (아군 방어력 증가)
    ARMOR_GUARD(204, "명예수방관", "동료에게 방어력의 150% 만큼의 갑옷을 입힙니다. 다음 턴까지 유지됩니다. 아군 사망 시 무효. (필요 마나: 35)", SkillCardType.ACTIVE),

    // 205 갑옷 (내 방어력 버프)
    ARMOR(205, "갑옷", "내 방어력의 150% 만큼의 갑옷을 입습니다. 이 갑옷은 1턴 동안 유지됩니다. (필요 마나: 25)", SkillCardType.ACTIVE),

    // 206 CPR (죽은 아군 부활)
    CPR(206, "CPR", "죽은 캐릭터를 HP 20%로 부활시킵니다. (필요 마나: 50)", SkillCardType.ACTIVE),

    // 207 회심의 일격 (단일 대상 공격력 증가)
    CRITICAL_HIT(207, "회심의 일격", "내 공격력의 1.8배로 대상에게 크게 공격합니다. (필요 마나: 50)", SkillCardType.ACTIVE),

    // 208 독가스 살포 (전체 대상 공격)
    POISON_GAS(208, "독가스 살포 (부장님 방귀)", "상대 몬스터 전체에 광역 공격을 가합니다. 공격력은 내 공격력 * 120% / N (대상 수). (필요 마나: 40)", SkillCardType.ACTIVE);


    private final int id;
    private final String displayName;
    private final String description; // 설명 필드
    private final SkillCardType type;

    ActiveSkillCard(int id, String displayName, String description, SkillCardType type) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
    }

    public static ActiveSkillCard from(int id) {
        for (ActiveSkillCard card : values()) {
            if (card.id == id) {
                return card;
            }
        }
        throw new BaseException(ErrorCode.NOT_VALID_ID, "유효하지 않은 Active Skill ID입니다: " + id);
    }
}
