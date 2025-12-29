package com.game.subwayworkerserver.toss.controller;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.toss.dto.GetAccessTokenRequest;
import com.game.subwayworkerserver.toss.dto.LogoutByUserKeyRequest;
import com.game.subwayworkerserver.toss.dto.RefreshTokenRequest;
import com.game.subwayworkerserver.toss.service.ApiResponse;
import com.game.subwayworkerserver.toss.service.TossAuthService;
import com.game.subwayworkerserver.toss.service.TossUserDataProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Profile("!dev")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TossAuthController {

    private final TossAuthService authService;
    private final TossUserDataProcessor tossUserDataProcessor;

    // Bearer 접두사 길이
    private static final int BEARER_PREFIX_LENGTH = 7;

    // Access Token 발급
    @PostMapping("/get-access-token")
    public ResponseEntity<?> getAccessToken(@RequestBody GetAccessTokenRequest request) {
        return ResponseEntity.ok(authService.getAccessToken(request));
    }

    // Access Token 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    // 사용자 정보 조회
    @GetMapping("/get-user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader);
        ApiResponse<Map<String, Object>> userInfo = authService.getUserInfo(accessToken);
        return ResponseEntity.ok(tossUserDataProcessor.process(userInfo));
    }

    // AccessToken으로 로그아웃
    @PostMapping("/logout-by-access-token")
    public ResponseEntity<?> logoutByAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader); // ⭐️ 헬퍼 메서드 사용
        return ResponseEntity.ok(authService.logoutByAccessToken(accessToken));
    }

    // UserKey로 로그아웃
    @PostMapping("/logout-by-user-key")
    public ResponseEntity<?> logoutByUserKey(@RequestBody LogoutByUserKeyRequest request) {
        return ResponseEntity.ok(authService.logoutByUserKey(request));
    }

    /**
     * Authorization 헤더에서 "Bearer "를 제거하고 Access Token만 추출합니다.
     * @param authorizationHeader "Bearer [token]" 형태의 헤더 값
     * @return 순수 Access Token 문자열
     * @throws BaseException 헤더가 유효하지 않을 경우
     */
    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Authorization 헤더가 'Bearer ' 형식으로 유효하게 전달되지 않았습니다.");
        }

        // "Bearer " (7글자) 이후의 토큰 추출
        return authorizationHeader.substring(BEARER_PREFIX_LENGTH);
    }
}