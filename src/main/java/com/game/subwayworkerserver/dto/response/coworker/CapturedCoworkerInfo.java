package com.game.subwayworkerserver.dto.response.coworker;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.dto.response.skill.SkillCardInfo;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import lombok.Builder;

import java.util.List;

@Builder
public record CapturedCoworkerInfo(
        @JsonUnwrapped CoworkerSummary summary,
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
        List<SkillCardInfo> activeSkills,
        List<SkillCardInfo> passiveSkills
) {
    public static CapturedCoworkerInfo of(final CoworkerEntity coworker, String position) {
        CoworkerType type = coworker.getCoworkerType();
        CoworkerSummary summary = new CoworkerSummary(
                coworker.getId(), type.name(), coworker.getName(), position, coworker.getLevel().getLevel(),
                coworker.getCoworkerElement().name(), coworker.getCoworkerElement().getKoreanName());
        return CapturedCoworkerInfo.builder()
                .summary(summary)
                .baseHpRange(range(type.getHpRange()))
                .baseInitManaRange(range(type.getMpNowRange()))
                .baseMaxManaRange(range(type.getMpMaxRange()))
                .baseAttackRange(range(type.getAttackRange()))
                .baseDefenseRange(range(type.getDefenseRange()))
                .baseManaRecoveryRange(String.valueOf(type.getManaRecovery()))
                .hp(coworker.getHp())
                .initMana(coworker.getInitMana())
                .maxMana(coworker.getMaxMana())
                .attack(coworker.getAttack())
                .defense(coworker.getDefense())
                .manaRecovery(coworker.getManaRecovery())
                .activeSkills(SkillCardInfo.activeSkills(coworker.getActiveSkill1(), coworker.getActiveSkill2()))
                .passiveSkills(SkillCardInfo.passiveSkills(coworker.getPassiveSkill1(), coworker.getPassiveSkill2()))
                .build();
    }

    private static String range(com.game.subwayworkerserver.util.CharacterGenerator.Range range) {
        return range.min() + "~" + range.max();
    }
}
