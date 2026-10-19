package com.game.subwayworkerserver.toss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
 import software.amazon.awssdk.regions.Region;
 import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
 import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
 import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

@Profile("!dev")
@Component
@RequiredArgsConstructor
public class TossSecretsLoader {

    private final TossAuthConfig tossAuthConfig;
    private final ObjectMapper objectMapper;

    public record MtlsCredentials(String clientCertPem, String clientKeyPem) {
    }

    /**
     * AWS Secrets Manager에서 mTLS 인증서와 개인 키를 로드합니다.
     */
    public MtlsCredentials getMtlsCredentials() {
        String secretName = "toss-partner-mtls-certs";
        Region region = Region.of("ap-southeast-2");

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
            String secret = getSecretValueResponse.secretString();

            @SuppressWarnings("unchecked")
            Map<String, String> secrets = objectMapper.readValue(secret, Map.class);

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
