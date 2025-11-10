package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutByAccessTokenRequest(

        // Authorization 헤더의 값을 담습니다.
        // Node.js의 if (!this.authorization) throw error; 역할을 수행합니다.
        @NotBlank(message = "Authorization 헤더가 필요합니다.")
        String authorization
) {
    // 참고: 이 DTO를 @RequestBody로 받지 않으므로,
    // 컨트롤러에서 헤더 값을 수동으로 바인딩해야 합니다.
}