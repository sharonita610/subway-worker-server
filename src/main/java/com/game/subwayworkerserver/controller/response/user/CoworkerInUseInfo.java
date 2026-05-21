package com.game.subwayworkerserver.controller.response.user;

import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

public record CoworkerInUseInfo(
    String engName,
    String korName,
    String position,
    int level,
    String engElement,
    String korElement
) {
    public static List<CoworkerInUseInfo> of(final List<CoworkerEntity> coworkerInUse) {
        return  coworkerInUse.stream()
                .map(coworker ->
                        new CoworkerInUseInfo(
                                coworker.getCoworkerType().name(),
                                coworker.getName(),
                                coworker.getLevel().getTitle(),
                                coworker.getLevel().getLevel(),
                                coworker.getCoworkerElement().name(),
                                coworker.getCoworkerElement().getKoreanName()
                        )
                ).toList();
    }
}
