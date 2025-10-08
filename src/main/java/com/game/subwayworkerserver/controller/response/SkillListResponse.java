package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record SkillListResponse(
        List<SkillBaseInfo> passiveSkillList,
        List<SkillBaseInfo> activeSkillList
) {
}
