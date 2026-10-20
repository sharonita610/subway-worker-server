package com.game.subwayworkerserver.dto.response.skill;

import java.util.List;

public record SkillListResponse(
        List<SkillBaseInfo> passiveSkillList,
        List<SkillBaseInfo> activeSkillList
) {
}
