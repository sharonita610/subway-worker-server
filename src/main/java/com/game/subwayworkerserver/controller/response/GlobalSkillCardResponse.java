package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record GlobalSkillCardResponse(
        List<GlobalSkillBaseInfo> skillList
) {
}
