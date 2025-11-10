package com.game.subwayworkerserver.toss.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutByUserKeyRequest(

        // Node.js의 if (!this.userKey) throw error; 역할을 수행
        @NotBlank(message = "userKey 값이 필요합니다.")
        String userKey
) {
    // 참고: 이 DTO를 사용하는 컨트롤러 메서드에 @Valid를 추가하여 유효성 검증을 활성화해야 합니다.
}