package com.game.subwayworkerserver.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PartnerConfigureRequest(

        @NotNull
        @Size(min = 1, max = 3, message = "파트너는 최소 1명, 최대 3명까지 지정 가능해요")
        List<Long> coworkerIds
) {
}
