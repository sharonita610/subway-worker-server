package com.game.subwayworkerserver.toss.util;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserInfoDecryptor {

    private final DecryptUserInfo externalDecryptor;
    private final TossAuthConfig envConfig;

    private static final String[] ENCRYPTED_FIELDS = {
            "ci", "name", "phone", "gender", "nationality", "birthday", "email"
    };

    public Map<String, Object> decrypt(Map<String, Object> encryptedUserInfoMap) {

        Map<String, Object> decryptedData = new HashMap<>(encryptedUserInfoMap);

        String keyBase64 = envConfig.getDecryptionKeyBase64();
        String aadString = envConfig.getAadString();

        for (String key : ENCRYPTED_FIELDS) {
            Object value = encryptedUserInfoMap.get(key);

            if (value instanceof String encryptedText && !encryptedText.isBlank()) {
                try {
                    String decryptedValue = externalDecryptor.decrypt(
                            encryptedText,
                            keyBase64,
                            aadString
                    );

                    decryptedData.put(key, decryptedValue);

                } catch (Exception e) {
                    // 복호화 실패는 시스템 오류로 간주하고 예외를 던집니다.
                    System.err.println("복호화 실패: Key=" + key + ", Error=" + e.getMessage());
                    throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 정보 복호화에 실패했습니다.");
                }
            } else {
                decryptedData.put(key, null);
            }
        }

        return decryptedData;
    }
}
