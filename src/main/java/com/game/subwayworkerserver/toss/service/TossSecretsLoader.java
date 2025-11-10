package com.game.subwayworkerserver.toss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossSecretsLoader { // SecretsLoader 대신 구체적인 이름 사용

    private final TossAuthConfig tossAuthConfig;
    private final ObjectMapper objectMapper;

    public record MtlsCredentials(String clientCertPem, String clientKeyPem) {
    }

    /**
     * AWS Secrets Manager에서 mTLS 인증서와 개인 키를 로드합니다.
     */
    public MtlsCredentials getMtlsCredentials() {
        String secretName = tossAuthConfig.getSecretName();
        // AP-NORTHEAST-2 (서울) Region을 가정합니다.
        Region region = Region.of("ap-northeast-2");

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        try {
            String secretJson = client.getSecretValue(request).secretString();

            // ⭐️ JSON 파싱 및 키 추출
            @SuppressWarnings("unchecked")
            Map<String, String> secrets = objectMapper.readValue(secretJson, Map.class);

            String certPem = secrets.get("client-cert-pem");
            String keyPem = secrets.get("client-key-pem");

            if (certPem == null || keyPem == null) {
                throw new RuntimeException("mTLS Secret JSON is missing required keys.");
            }

            return new MtlsCredentials(certPem, keyPem);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load mTLS Secret from AWS: " + secretName, e);
        }
    }
}
