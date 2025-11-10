package com.game.subwayworkerserver.toss.controller;

import com.game.subwayworkerserver.toss.TLSClient;
import com.game.subwayworkerserver.toss.service.ApiResponse;
import com.game.subwayworkerserver.toss.service.TossSecretsLoader;
import com.game.subwayworkerserver.toss.util.TossAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.net.ssl.SSLContext;

@Component
@RequiredArgsConstructor
public class TossApiClient {

    private final TossAuthConfig tossAuthConfig;
    private final TossSecretsLoader tossSecretsLoader;
    private final TLSClient tlsClient;

    private final SSLContext sslContext = initializeSslContext();
    private static final String API_VERSION = "/v1/apps-in-toss/user/oauth2";
    private static final String CONTENT_TYPE = "application/json";

    /**
     * mTLS 설정을 초기화하고 SSLContext를 생성합니다. (로직 유지)
     */
    private SSLContext initializeSslContext() {
        try {
            TossSecretsLoader.MtlsCredentials credentials = tossSecretsLoader.getMtlsCredentials();
            return tlsClient.createSSLContext(credentials.clientCertPem(), credentials.clientKeyPem());
        } catch (Exception e) {
            throw new RuntimeException("mTLS SSLContext 초기화에 실패했습니다.", e);
        }
    }

    // --- POST 요청 (AccessToken 발급/Refresh) ---
    public ApiResponse<Map<String, Object>> post(String endpointPath, Object requestBody) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);

        // Node.js의 client.post(url, body) 역할
        return tlsClient.sendPostRequest( // ⭐️ TLSClient에 sendPostRequest(url, body, contentType, sslContext) 메서드 가정
                fullUrl,
                requestBody,
                CONTENT_TYPE,
                sslContext
        );
    }

    // --- GET 요청 (User Info) ---
    public ApiResponse<Map<String, Object>> get(String endpointPath, String accessToken) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + accessToken);

        // ⭐️ [수정] GET 요청 전용 메서드 호출 (headers를 포함해야 함)
        return tlsClient.sendGetRequest( // ⭐️ TLSClient에 sendGetRequest(url, headers, sslContext) 메서드 가정
                fullUrl,
                headers,
                sslContext
        );
    }

    // --- POST 요청 (Header Only - Logout) ---
    public ApiResponse<Map<String, Object>> postWithHeader(String endpointPath, String accessToken) throws Exception {
        String fullUrl = tossAuthConfig.getAuthApiUrl(API_VERSION + endpointPath);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + accessToken);

        // Node.js의 client.post(url, {}, headers) 역할
        return tlsClient.sendPostRequest( // ⭐️ TLSClient에 sendPostRequest(url, body, contentType, headers, sslContext) 메서드 가정
                fullUrl,
                null, // 빈 본문
                CONTENT_TYPE,
                headers,
                sslContext
        );
    }
}