package com.game.subwayworkerserver.domain.coworker;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;

import java.util.List;

public record CoworkerTotal(
        // 기본 정보
        long coworkerId,
        String engName,
        String korName,
        boolean inUse,
        boolean onSale,
        String position,
        String element,

        int level,
        int exp,

        long hp,
        long attack,
        long defense,
        long initMana,
        long maxMana,
        long manaRecovery,
        long treasureHp,
        long treasureAttack,
        long treasureDefense,
        long treasureMana,
        long treasureMaxMana,
        long treasureManaRecovery,

        int activeSkillId1,
        int activeSkillId2,
        int passiveSkillId1,
        int passiveSkillId2,

        List<ActiveSkillCard> activeSkills,
        List<PassiveSkillCard> passiveSkills
) {
}
