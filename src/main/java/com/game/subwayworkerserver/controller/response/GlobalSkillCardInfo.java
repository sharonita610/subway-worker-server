package com.game.subwayworkerserver.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record GlobalSkillCardInfo(
        int price,
        long sellerId,
        long marketId,
        SkillCardBaseInfo skillCardBaseInfo
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SkillCardBaseInfo(
            long id,
            String korName,
            String engName,
            String description,
            String skillType,
            int quantity,
            int currentLevel,
            int maxLevel,
            boolean inUse,
            boolean onSale
    ) {
    }
}
