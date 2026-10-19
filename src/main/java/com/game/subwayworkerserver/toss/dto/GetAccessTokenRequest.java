package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record GetAccessTokenRequest(

        @NotBlank(message = "authorizationCode가 필요합니다.")
        String authorizationCode,

        @NotBlank(message = "referrer가 필요합니다.")
        String referrer
) {
}