package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record GetUserInfoRequest(

        // Authorization 헤더의 값을 담습니다.
        // Node.js의 if (!this.authorization) throw error; 역할을 수행합니다.
        @NotBlank(message = "Authorization 헤더가 필요합니다.")
        String authorization
) {
    // 참고: Spring 환경에서는 이 DTO를 @RequestBody 대신,
    // 컨트롤러에서 @RequestHeader를 통해 수동으로 생성하거나 검증합니다.
}