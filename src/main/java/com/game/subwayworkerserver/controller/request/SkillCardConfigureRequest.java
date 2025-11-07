package com.game.subwayworkerserver.controller.request;

import jakarta.validation.constraints.NotNull;

public record SkillCardConfigureRequest (

        long skillCardId,

        @NotNull
        String skillCardType,

        @NotNull
        long coworkerId
){
}
