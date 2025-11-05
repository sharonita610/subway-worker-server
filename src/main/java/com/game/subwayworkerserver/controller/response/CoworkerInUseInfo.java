package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record CoworkerInUseInfo(
    String engName,
    String korName,
    String position,
    int level
) {
    public static List<CoworkerInUseInfo> of(final List<CoworkerEntity> coworkerInUse) {
        return  coworkerInUse.stream()
                .map(coworker ->
                        new CoworkerInUseInfo(
                                coworker.getCoworkerType().name(),
                                coworker.getName(),
                                coworker.getLevel().getTitle(),
                                coworker.getLevel().getLevel()
                        )
                ).toList();
    }
}
