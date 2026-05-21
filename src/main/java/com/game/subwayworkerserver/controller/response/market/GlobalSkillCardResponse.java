package com.game.subwayworkerserver.controller.response.market;

import com.game.subwayworkerserver.domain.market.SkillCardOnSale;

import java.util.List;

public record GlobalSkillCardResponse(
        List<GlobalSkillCardInfo> skillCardList
) {
    public static GlobalSkillCardResponse of(final List<SkillCardOnSale> filteredCoworkers) {
        return new GlobalSkillCardResponse(
                filteredCoworkers.stream()
                        .map(GlobalSkillCardInfo::of)
                        .toList()
        );
    }
}
