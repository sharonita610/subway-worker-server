package com.game.subwayworkerserver.dto.request.coworker;

import jakarta.validation.constraints.NotNull;

public record SkillCardConfigureRequest (

        long skillCardId,

        @NotNull
        String skillCardType,

        @NotNull
        long coworkerId
){
}
