package com.game.subwayworkerserver.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillCardConfigureRequest (

        @Size(min = 0, max = 1, message = "스킬카드는 타입 당 한 장만 등록 가능해요.")
        long skillCardId,

        String skillCardType,
        @NotNull
        long coworkerId
){
}
