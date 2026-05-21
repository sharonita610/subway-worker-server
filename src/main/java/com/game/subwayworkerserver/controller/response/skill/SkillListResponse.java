package com.game.subwayworkerserver.controller.response.skill;

import java.util.List;

public record SkillListResponse(
        List<SkillBaseInfo> passiveSkillList,
        List<SkillBaseInfo> activeSkillList
) {
}
