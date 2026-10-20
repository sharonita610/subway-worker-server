package com.game.subwayworkerserver.dto.response.coworker;

import com.game.subwayworkerserver.dto.response.skill.SkillBaseInfo;
import com.game.subwayworkerserver.domain.baseinfo.*;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import lombok.Builder;

import java.util.List;

@Builder
public record CapturedCoworkerInfo(
        String coworkerName,
        String position,
        String baseHpRange,
        String baseInitManaRange,
        String baseMaxManaRange,
        String baseAttackRange,
        String baseDefenseRange,
        String baseManaRecoveryRange,
        long hp,
        long initMana,
        long maxMana,
        long attack,
        long defense,
        long manaRecovery,
        List<SkillBaseInfo> activeSkills,
        List<SkillBaseInfo> passiveSkills,
        String engElementName,
        String korElementName
) {

    public static CapturedCoworkerInfo of(final CoworkerEntity coworker, String position) {
        CoworkerType type = coworker.getCoworkerType();
        return CapturedCoworkerInfo.builder()
                .coworkerName(coworker.getName())
                .position(position)
                .baseHpRange(type.getHpRange().min() + "~"  + type.getHpRange().max())
                .baseInitManaRange(type.getMpNowRange().min() + "~"  + type.getMpNowRange().max())
                .baseMaxManaRange(type.getMpMaxRange().min() + "~"  + type.getMpMaxRange().max())
                .baseAttackRange(type.getAttackRange().min() + "~" + type.getAttackRange().max())
                .baseDefenseRange(type.getDefenseRange().min() + "~" + type.getDefenseRange().max())
                .baseManaRecoveryRange(String.valueOf(type.getManaRecovery()))
                .hp(coworker.getHp())
                .initMana(coworker.getInitMana())
                .maxMana(coworker.getMaxMana())
                .attack(coworker.getAttack())
                .defense(coworker.getDefense())
                .manaRecovery(coworker.getManaRecovery())
                .activeSkills(SkillBaseInfo.activeSkill(coworker.getActiveSkill1(), coworker.getActiveSkill2()))
                .passiveSkills(SkillBaseInfo.passiveSkill(coworker.getPassiveSkill1(), coworker.getPassiveSkill2()))
                .engElementName(coworker.getCoworkerElement().name())
                .korElementName(coworker.getCoworkerElement().getKoreanName())
                .build();
    }
}
