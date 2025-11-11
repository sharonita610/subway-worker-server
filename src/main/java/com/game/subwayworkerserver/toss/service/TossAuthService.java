package com.game.subwayworkerserver.toss.service;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.toss.controller.TossApiClient;
import com.game.subwayworkerserver.toss.dto.GetAccessTokenRequest;
import com.game.subwayworkerserver.toss.dto.LogoutByUserKeyRequest;
import com.game.subwayworkerserver.toss.dto.RefreshTokenRequest;
import com.game.subwayworkerserver.toss.util.DecryptUserInfo;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TossAuthService {

    private final DecryptUserInfo decryptUserInfo;
    private final TossApiClient tossApiClient;
    private final TossAuthConfig tossAuthConfig;

    // 🚨 함수형 인터페이스 정의 (API 호출 람다를 받기 위함)
    @FunctionalInterface
    private interface ApiCallSupplier<T> {
        T get() throws Throwable;
    }

    // 🚨 예외 처리를 위한 헬퍼 메서드
    private <T> ApiResponse<T> handleApiCall(ApiCallSupplier<ApiResponse<T>> supplier, String apiName) {
        try {
            return supplier.get();
        } catch (BaseException e) {
            throw e;
        } catch (Throwable e) {
            System.err.println(apiName + " API 호출 실패: " + e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, apiName + " 처리 중 외부 시스템 오류가 발생했습니다.");
        }
    }

    // AccessToken 발급
    public ApiResponse<Map<String, Object>> getAccessToken(GetAccessTokenRequest request) {
        return handleApiCall(() -> {
            ApiResponse<Map<String, Object>> post = tossApiClient.post("/generate-token", request);
            System.out.println("Toss API 응답: " + post.data());
            return post;
        }, "AccessToken 발급");
    }

    // AccessToken 재발급
    public ApiResponse<Map<String, Object>> refreshAccessToken(RefreshTokenRequest request) {
        return handleApiCall(() -> {
            return tossApiClient.post("/refresh-token", request);
        }, "AccessToken 재발급");
    }

    // 사용자 정보 조회 (복호화 포함)
    public ApiResponse<Map<String, Object>> getUserInfo(String accessToken) {
        return handleApiCall(() -> {
            // 1. 외부 API 호출
            ApiResponse<Map<String, Object>> response = tossApiClient.get("/login-me", accessToken);

            // 2. 복호화 로직
            Map<String, Object> parsedData = response.data();

            // Map에서 'success' 필드를 안전하게 추출하고 String 타입인지 확인
            if (parsedData != null && parsedData.get("success") instanceof String encryptedData && !encryptedData.isBlank()) {

                // ⭐️ [수정 완료] 3개의 인자를 전달하여 DecryptUserInfo.decrypt 시그니처와 일치시킵니다.
                Object decryptedUser = decryptUserInfo.decrypt(
                        encryptedData,
                        tossAuthConfig.getDecryptionKeyBase64(), // ⭐️ 키 전달
                        tossAuthConfig.getAadString()            // ⭐️ AAD 전달
                );
                System.out.println("decryptedUser = " + decryptedUser);
                for (Map.Entry<String, Object> stringObjectEntry : parsedData.entrySet()) {
                    System.out.println(stringObjectEntry.getKey() + " = " + stringObjectEntry.getValue());
                }
                // 복호화된 데이터를 응답 Map에 덮어쓰기
                parsedData.put("success", decryptedUser);
            } else {
                // 암호화 데이터가 없거나 형식이 잘못된 경우
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "토스 사용자 정보 복호화 대상 필드(success)가 유효하지 않습니다.");
            }

            return response;
        }, "사용자 정보 조회");
    }

    // AccessToken으로 로그아웃
    public ApiResponse<Map<String, Object>> logoutByAccessToken(String accessToken) {
        return handleApiCall(() -> {
            return tossApiClient.postWithHeader("/access/remove-by-access-token", accessToken);
        }, "Access Token 로그아웃");
    }

    // UserKey로 로그아웃
    public ApiResponse<Map<String, Object>> logoutByUserKey(LogoutByUserKeyRequest request) {
        return handleApiCall(() -> {
            return tossApiClient.post("/access/remove-by-user-key", request);
        }, "User Key 로그아웃");
    }
}