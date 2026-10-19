package com.game.subwayworkerserver.toss.controller;

import com.game.subwayworkerserver.toss.TLSClient;
import com.game.subwayworkerserver.toss.service.ApiResponse;
import com.game.subwayworkerserver.toss.service.TossSecretsLoader;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.net.ssl.SSLContext;

@Profile("!dev")
@Component
@RequiredArgsConstructor
public class TossApiClient {

    private final TossAuthConfig tossAuthConfig;
    private final TossSecretsLoader tossSecretsLoader;
    private final TLSClient tlsClient;

    private SSLContext sslContext;
    private static final String API_VERSION = "/v1/apps-in-toss/user/oauth2";
    private static final String CONTENT_TYPE = "application/json";

    @PostConstruct
    private void init() {
        this.sslContext = initializeSslContext();
    }
    /**
     * mTLS 설정을 초기화하고 SSLContext를 생성합니다.
     */
    private SSLContext initializeSslContext() {
        try {
            TossSecretsLoader.MtlsCredentials credentials = tossSecretsLoader.getMtlsCredentials();
            return tlsClient.createSSLContext(credentials.clientCertPem(), credentials.clientKeyPem());
        } catch (Exception e) {
            throw new RuntimeException("mTLS SSLContext 초기화에 실패했습니다.", e);
        }
    }

    public ApiResponse<Map<String, Object>> post(String endpointPath, Object requestBody) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);

        return tlsClient.sendPostRequest(
                fullUrl,
                requestBody,
                CONTENT_TYPE,
                sslContext
        );
    }

    public ApiResponse<Map<String, Object>> get(String endpointPath, String accessToken) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + accessToken);

        return tlsClient.sendGetRequest(
                fullUrl,
                headers,
                sslContext
        );
    }

    public ApiResponse<Map<String, Object>> postWithHeader(String endpointPath, String accessToken) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + accessToken);

        return tlsClient.sendPostRequest(
                fullUrl,
                null,
                CONTENT_TYPE,
                headers,
                sslContext
        );
    }
}