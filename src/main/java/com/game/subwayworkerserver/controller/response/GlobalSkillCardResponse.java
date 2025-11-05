package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

public record GlobalSkillCardResponse(
        List<GlobalSkillCardInfo> skillCardList
) {
    public static GlobalSkillCardResponse of(List<SkillCardOnSale> filteredCoworkers) {
        return null;
    }
}
