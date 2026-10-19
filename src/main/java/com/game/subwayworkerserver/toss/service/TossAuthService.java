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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Profile("!dev")
@Slf4j
@Service
@RequiredArgsConstructor
public class TossAuthService {

    private final DecryptUserInfo decryptUserInfo;
    private final TossApiClient tossApiClient;
    private final TossAuthConfig tossAuthConfig;

    @FunctionalInterface
    private interface ApiCallSupplier<T> {
        T get() throws Throwable;
    }

    private <T> ApiResponse<T> handleApiCall(ApiCallSupplier<ApiResponse<T>> supplier, String apiName) {
        try {
            return supplier.get();
        } catch (BaseException e) {
            throw e;
        } catch (Throwable e) {
            log.error("{} API 호출 실패: {}", apiName, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, apiName + " 처리 중 외부 시스템 오류가 발생했습니다.");
        }
    }

    public ApiResponse<Map<String, Object>> getAccessToken(GetAccessTokenRequest request) {
        return handleApiCall(() -> {
            ApiResponse<Map<String, Object>> post = tossApiClient.post("/generate-token", request);
            log.info("Toss API 응답 정상 반환.");
            return post;
        }, "AccessToken 발급");
    }

    public ApiResponse<Map<String, Object>> refreshAccessToken(RefreshTokenRequest request) {
        return handleApiCall(() -> tossApiClient.post("/refresh-token", request), "AccessToken 재발급");
    }

    public ApiResponse<Map<String, Object>> getUserInfo(String accessToken) {
        return handleApiCall(() -> {
            ApiResponse<Map<String, Object>> response = tossApiClient.get("/login-me", accessToken);
            Map<String, Object> parsedData = response.data();

            if (parsedData != null && parsedData.get("success") instanceof Map successMap) {

                String key = tossAuthConfig.getDecryptionKeyBase64();
                String aad = tossAuthConfig.getAadString();

                String[] fieldsToDecrypt = {"name", "gender", "email"};

                for (String fieldName : fieldsToDecrypt) {

                    if (successMap.get(fieldName) instanceof String encryptedData && !encryptedData.isBlank()) {

                        Object decryptedValue = decryptUserInfo.decrypt(encryptedData, key, aad);

                        successMap.put(fieldName, decryptedValue);
                    }
                }
                parsedData.put("success", successMap);

            } else {
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "토스 사용자 정보의 'success' 필드가 유효하지 않거나 형식이 다릅니다.");
            }

            return response;
        }, "사용자 정보 조회");
    }

    public ApiResponse<Map<String, Object>> logoutByAccessToken(String accessToken) {
        return handleApiCall(() -> {
            return tossApiClient.postWithHeader("/access/remove-by-access-token", accessToken);
        }, "Access Token 로그아웃");
    }

    public ApiResponse<Map<String, Object>> logoutByUserKey(LogoutByUserKeyRequest request) {
        return handleApiCall(() -> {
            return tossApiClient.post("/access/remove-by-user-key", request);
        }, "User Key 로그아웃");
    }
}