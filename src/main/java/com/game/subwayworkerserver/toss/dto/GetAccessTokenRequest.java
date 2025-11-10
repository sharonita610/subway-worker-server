package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GetAccessTokenRequest(

        // Node.js의 if (!this.authorizationCode) throw error; 역할을 수행
        @NotBlank(message = "authorizationCode가 필요합니다.")
        String authorizationCode,

        // Node.js의 if (!this.referrer) throw error; 역할을 수행
        @NotBlank(message = "referrer가 필요합니다.")
        String referrer
) {
    // 참고: Spring 환경에서 @NotBlank를 사용하려면,
    // 컨트롤러 메서드에 @Valid 어노테이션을 추가해야 합니다.
}