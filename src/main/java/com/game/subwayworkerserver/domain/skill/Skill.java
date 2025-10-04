package com.game.subwayworkerserver.domain.skill;

import java.util.List;

public record Skill(
        List<PassiveSkill> passiveSkills,
        List<ActiveSkill> activeSkills
) {

    public List<PassiveSkill> getEquippedPassiveSkill() {
        return passiveSkills.stream()
                .filter(PassiveSkill::inUse)
                .toList();
    }

    public List<ActiveSkill> getEquippedActiveSkill() {
        return activeSkills.stream()
                .filter(ActiveSkill::inUse)
                .toList();
    }
}
