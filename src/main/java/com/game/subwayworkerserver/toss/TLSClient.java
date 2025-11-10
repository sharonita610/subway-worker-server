package com.game.subwayworkerserver.toss;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON 처리를 위해 필요
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.game.subwayworkerserver.toss.service.ApiResponse;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@Component
public class TLSClient { // 실제 파일 이름은 TossTLSClient 등이 될 수 있습니다.

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파서

    // 🚨 [필수] Node.js의 loadCertificate/loadPrivateKey를 대체하는 메서드가 존재한다고 가정
    // public SSLContext createSSLContext(String certPem, String keyPem) { ... }

    // ----------------------------------------------------
    // --- 1. POST with BODY (Access Token 발급, Refresh) ---
    // ----------------------------------------------------
    public ApiResponse<Map<String, Object>> sendPostRequest(
            String fullUrl,
            Object requestBody,
            String contentType,
            SSLContext sslContext
    ) throws Exception {
        // Node.js의 makeRequest(url, 'POST', data, headers) 역할
        return executeRequest(fullUrl, "POST", contentType, requestBody, null, sslContext);
    }

    // ----------------------------------------------------
    // --- 2. POST with HEADERS (Logout by Access Token) ---
    // ----------------------------------------------------
    public ApiResponse<Map<String, Object>> sendPostRequest(
            String fullUrl,
            Object requestBody, // null을 전달받음
            String contentType,
            Map<String, String> headers, // 추가 헤더 받음
            SSLContext sslContext
    ) throws Exception {
        // Node.js의 makeRequest(url, 'POST', data=null, headers) 역할
        return executeRequest(fullUrl, "POST", contentType, requestBody, headers, sslContext);
    }

    // ----------------------------------------------------
    // --- 3. GET Request (User Info) ---
    // ----------------------------------------------------
    public ApiResponse<Map<String, Object>> sendGetRequest(
            String fullUrl,
            Map<String, String> headers,
            SSLContext sslContext
    ) throws Exception {
        // Node.js의 client.get(url, headers) 역할
        return executeRequest(fullUrl, "GET", CONTENT_TYPE, null, headers, sslContext);
    }

    /**
     * 최종 HTTP 요청을 실행하는 내부 헬퍼 메서드 (mTLS 포함)
     */
    private ApiResponse<Map<String, Object>> executeRequest(
            String url,
            String method,
            String contentType,
            Object body,
            Map<String, String> headers,
            SSLContext context
    ) throws Exception {

        URL requestUrl = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection();

        try {
            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 헤더 설정
            connection.setRequestProperty("Content-Type", contentType);
            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }

            if (body != null) {
                connection.setDoOutput(true);
                String jsonBody = objectMapper.writeValueAsString(body);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = connection.getResponseCode();

            // 응답 스트림 읽기
            InputStream stream = (statusCode >= 200 && statusCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                responseBody = reader.lines().collect(Collectors.joining("\n"));
            }

            // 응답 파싱
            if (statusCode != 200) {
                // 토스 API가 에러 시에도 JSON을 반환한다고 가정하고 에러를 BaseException으로 변환
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "외부 API 오류: " + responseBody);
            }

            // 💡 Node.js의 JSON.parse(response.data) 역할
            Map<String, Object> data = objectMapper.readValue(responseBody, new TypeReference<>() {});

            return new ApiResponse<>(statusCode, "성공", data);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public SSLContext createSSLContext(String certPem, String keyPem) throws Exception {

        // 1. PEM 문자열을 Java 객체로 로드
        X509Certificate cert = loadCertificate(certPem);
        PrivateKey key = loadPrivateKey(keyPem);

        // 2. KeyStore 생성 및 인증서/키 설정
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null); // 빈 KeyStore 로드

        char[] password = "".toCharArray(); // 개인 키에 암호가 없다고 가정
        keyStore.setCertificateEntry("client-cert", cert);
        keyStore.setKeyEntry("client-key", key, password, new X509Certificate[]{cert});

        // 3. KeyManagerFactory 초기화
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        // 4. SSLContext 초기화
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null); // TrustManager는 기본값 사용

        return context;
    }

// --------------------------------------------------------------------------------

    /**
     * PEM 문자열에서 X509 인증서를 로드합니다.
     */
    private X509Certificate loadCertificate(String certPem) throws Exception {
        String encoded = certPem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", ""); // 모든 공백 제거

        byte[] bytes = Base64.getDecoder().decode(encoded);

        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(bytes));
    }

    /**
     * PEM 문자열에서 PKCS8 개인 키를 로드합니다.
     */
    private PrivateKey loadPrivateKey(String keyPem) throws Exception {
        String encoded = keyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // 모든 공백 제거

        byte[] bytes = Base64.getDecoder().decode(encoded);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);

        // 알고리즘은 RSA를 가정합니다.
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}