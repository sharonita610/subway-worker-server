package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record StageCoworkerListResponse(
        long id,
        String type,
        String name,
        String element,
        HpInfo hp,
        MpInfo mp,
        int attack,
        int defense,
        List<SkillBaseInfo> passiveSkill,
        List<SkillBaseInfo> activeSkill,
        ResurrectionInfo resurrection
) {
    private record HpInfo(
            int nowHp,
            int maxHp,
            int hpRecover,
            int protectSuit,
            int armor
    ) {
    }

    private record MpInfo(
            int nowMp,
            int maxMp,
            int hpRecover
    ) {
    }

    private static class ResurrectionInfo {
    }
}
