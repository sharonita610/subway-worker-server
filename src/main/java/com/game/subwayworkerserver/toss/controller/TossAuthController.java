package com.game.subwayworkerserver.toss.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Toss Auth", description = "토스 로그인 연동 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TossAuthController {

    private final TossAuthService authService;
    private final TossUserDataProcessor tossUserDataProcessor;

    // Bearer 접두사 길이
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Operation(summary = "토스 액세스 토큰 발급", description = "토스 로그인 인가 코드로 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    @PostMapping("/get-access-token")
    public ResponseEntity<?> getAccessToken(@RequestBody GetAccessTokenRequest request) {
        return ResponseEntity.ok(authService.getAccessToken(request));
    }

    @Operation(summary = "토스 액세스 토큰 갱신", description = "리프레시 토큰으로 액세스 토큰을 재발급합니다.")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    @Operation(summary = "토스 사용자 정보 조회", description = "Authorization 헤더의 토스 액세스 토큰으로 암호화된 사용자 정보를 받아 복호화해 돌려줍니다.")
    @GetMapping("/get-user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader);
        ApiResponse<Map<String, Object>> userInfo = authService.getUserInfo(accessToken);
        return ResponseEntity.ok(tossUserDataProcessor.process(userInfo));
    }

    @Operation(summary = "토스 로그아웃 (액세스 토큰)", description = "Authorization 헤더의 액세스 토큰으로 토스 세션을 종료합니다.")
    @PostMapping("/logout-by-access-token")
    public ResponseEntity<?> logoutByAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader);
        return ResponseEntity.ok(authService.logoutByAccessToken(accessToken));
    }

    @Operation(summary = "토스 로그아웃 (사용자 키)", description = "토스 사용자 키로 해당 사용자의 토스 세션을 종료합니다.")
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