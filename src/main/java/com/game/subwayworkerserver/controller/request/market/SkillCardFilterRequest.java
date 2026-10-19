package com.game.subwayworkerserver.controller.request.market;

import java.util.List;

public record SkillCardFilterRequest(
        List<Integer> levels,
        List<String> passiveSkills,
        List<String> activeSkills,
        String sortType
) {
}
