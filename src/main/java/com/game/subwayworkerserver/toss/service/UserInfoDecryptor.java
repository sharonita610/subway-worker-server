package com.game.subwayworkerserver.toss.service;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.toss.util.DecryptUserInfo;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserInfoDecryptor {

    private final DecryptUserInfo externalDecryptor; // Node.js의 decryptUserData 역할
    private final TossAuthConfig tossAuthConfig;      // DECRYPTION_KEY_BASE64, AAD_STRING 역할

    // Node.js의 fields 배열 역할: 복호화할 필드 목록
    private static final String[] ENCRYPTED_FIELDS = {
            "ci", "name", "phone", "gender", "nationality", "birthday", "email"
    };

    /**
     * 암호화된 사용자 정보 Map을 받아 지정된 필드만 복호화하여 결합된 Map을 반환합니다.
     * (Node.js의 decryptUserInfo 함수 역할)
     * * @param encryptedUserInfoMap 암호화된 사용자 정보 (Map 형태)
     * @return 복호화된 필드가 포함된 Map
     */
    public Map<String, Object> decryptUserInfo(Map<String, Object> encryptedUserInfoMap) {

        // 원본 데이터를 복사하여 결과를 담을 Map을 생성 (Node.js의 {...userInfo, ...decrypted} 역할)
        Map<String, Object> decryptedData = new HashMap<>(encryptedUserInfoMap);

        String keyBase64 = tossAuthConfig.getDecryptionKeyBase64();
        String aadString = tossAuthConfig.getAadString();

        for (String key : ENCRYPTED_FIELDS) {

            // Node.js의 userInfo?.[key] 역할
            Object value = encryptedUserInfoMap.get(key);

            // Node.js의 typeof value === 'string' && value !== null 역할
            if (value instanceof String encryptedText && !encryptedText.isBlank()) {
                try {
                    // ⭐️ 외부 복호화 함수 호출 (Node.js의 decryptUserData 호출)
                    String decryptedValue = externalDecryptor.decrypt(
                            encryptedText,
                            keyBase64,
                            aadString
                    );

                    // 복호화된 값을 Map에 덮어쓰기
                    decryptedData.put(key, decryptedValue);

                } catch (Exception e) {
                    // 복호화 실패는 치명적 시스템 오류
                    System.err.println("사용자 정보 복호화 실패: Key=" + key + ", Error=" + e.getMessage());
                    throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 정보 복호화에 실패했습니다.");
                }
            } else {
                // 값이 없거나 String이 아니면 (Node.js의 null 처리)
                decryptedData.put(key, null);
            }
        }

        return decryptedData;
    }
}