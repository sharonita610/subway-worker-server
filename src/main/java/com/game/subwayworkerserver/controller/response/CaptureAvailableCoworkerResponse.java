package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;

public record CaptureAvailableCoworkerResponse(
        String coworkerId,
        String coworkerName,
        String coworkerPosition,
        int coworkerLevel
) {

    public static CaptureAvailableCoworkerResponse of(
            final String coworkerId, final CoworkerElement coworkerElement, final CoworkerLevel level
    ) {
        return new CaptureAvailableCoworkerResponse(coworkerId, coworkerElement.getKoreanName(), level.getTitle(), level.getLevel());
    }

    public static CaptureAvailableCoworkerResponse of(
            final String coworkerId, final String name, final String position, final int level
    ) {
        return new CaptureAvailableCoworkerResponse(coworkerId, name, position, level);
    }
}
