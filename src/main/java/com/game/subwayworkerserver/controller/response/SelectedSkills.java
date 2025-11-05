package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record SelectedSkills(
        List<SkillBaseInfo> passiveSkills,
        List<SkillBaseInfo> activeSkills
) {
    public static SelectedSkills of(CoworkerEntity coworker) {

        return null;
    }

    public static SelectedSkills of(CoworkerOnSale coworkerOnSale) {

        return null;
    }
}
