package com.game.subwayworkerserver.toss.service;

public record ApiResponse<T>(
        // HTTP 상태 코드 (200, 400, 500 등)
        int statusCode,

        // 응답 메시지 또는 에러 메시지
        String message,

        // 실제 응답 데이터 (복호화된 유저 정보, 토큰, Map 등)
        T data
) {
    // 성공 응답을 위한 팩토리 메서드 등을 포함할 수 있습니다.
}
