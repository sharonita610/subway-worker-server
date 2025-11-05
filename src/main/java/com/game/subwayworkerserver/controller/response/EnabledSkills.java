package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record EnabledSkills(
        List<SkillBaseInfo> passiveSkills,
        List<SkillBaseInfo> activeSkills
) {
}
