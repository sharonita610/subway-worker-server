package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutByAccessTokenRequest(

        @NotBlank(message = "Authorization 헤더가 필요합니다.")
        String authorization
) {
}