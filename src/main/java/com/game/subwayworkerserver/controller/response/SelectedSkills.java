package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record SelectedSkills(
        List<SkillBaseInfo> passiveSkills,
        List<SkillBaseInfo> activeSkills
) {
}
