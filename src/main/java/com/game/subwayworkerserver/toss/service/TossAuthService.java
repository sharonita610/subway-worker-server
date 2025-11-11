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
            System.out.println("response.data() = " + response.data());
            // 2. 복호화 로직
            Map<String, Object> parsedData = response.data();

            // Map에서 'success' 필드를 안전하게 추출하고 String 타입인지 확인
            if (parsedData != null && parsedData.get("success") instanceof Map successMap) {

                // 4. 복호화에 필요한 키와 AAD를 미리 준비합니다.
                String key = tossAuthConfig.getDecryptionKeyBase64();
                String aad = tossAuthConfig.getAadString();

                // 5. ⭐️ 복호화할 필드 목록 (로그에 보이는 암호화된 필드들)
                String[] fieldsToDecrypt = {"name", "gender", "email"};

                // 6. ⭐️ 'success' 맵 내부를 순회하며 복호화
                for (String fieldName : fieldsToDecrypt) {

                    // 7. 해당 필드가 암호화된 String 값인지 확인
                    if (successMap.get(fieldName) instanceof String encryptedData && !encryptedData.isBlank()) {

                        // 8. 복호화 실행
                        Object decryptedValue = decryptUserInfo.decrypt(encryptedData, key, aad);

                        // 9. 복호화된 값으로 덮어쓰기
                        successMap.put(fieldName, decryptedValue);
                        System.out.println("복호화 [" + fieldName + "] = " + decryptedValue);
                    }
                }

                // 10. (선택사항) 수정된 successMap을 다시 parsedData에 넣습니다. (이미 참조 중이라 안 해도 되지만 명시적으로)
                parsedData.put("success", successMap);

            } else {
                // 'success' 필드가 없거나 Map이 아닌 경우
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "토스 사용자 정보의 'success' 필드가 유효하지 않거나 형식이 다릅니다.");
            }

            // 11. 복호화된 데이터가 포함된 전체 응답(response) 반환
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