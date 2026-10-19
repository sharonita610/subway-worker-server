package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutByUserKeyRequest(

        @NotBlank(message = "userKey 값이 필요합니다.")
        String userKey
) {
}