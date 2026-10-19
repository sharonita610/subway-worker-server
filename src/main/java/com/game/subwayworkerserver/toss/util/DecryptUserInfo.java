package com.game.subwayworkerserver.toss.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Component
public class DecryptUserInfo {

    private static final int IV_LENGTH = 12;    // 초기화 벡터 (Nonce) 길이
    private static final int TAG_LENGTH_BITS = 128; // 인증 태그 길이 (16 바이트 * 8 = 128 비트)
    private static final int TAG_LENGTH_BYTES = 16; // 인증 태그 길이 (16 바이트)

    /**
     * AES-256-GCM 복호화 함수
     *
     * @param encryptedBase64  - 암호문 (Base64 인코딩, IV + 암호문 + 태그 순서)
     * @param base64EncodedKey - AES 키 (Base64)
     * @param aad              - AAD 문자열
     * @returns 복호화된 평문
     */
    public String decrypt(
            String encryptedBase64,
            String base64EncodedKey,
            String aad
    ) throws Exception {

        byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
        byte[] keyByteArray = Base64.getDecoder().decode(base64EncodedKey);

        // IV는 decoded의 첫 12바이트
        byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);

        // 암호문과 태그는 IV 다음부터 끝까지 (복호화 시 Cipher.doFinal에 전체를 전달)
        final int cipherTextAndTagLength = decoded.length - IV_LENGTH;

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(keyByteArray, "AES");

        // GCMParameterSpec은 태그 길이를 비트 단위로 받습니다.
        GCMParameterSpec nonceSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, nonceSpec);

        cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));

        // Java에서는 IV 다음 부분부터 (IV_LENGTH) 암호문과 태그를 포함한 전체 바이트를 doFinal에 전달합니다.
        byte[] decrypted = cipher.doFinal(
                decoded,
                IV_LENGTH,
                cipherTextAndTagLength
        );

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
