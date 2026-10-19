package com.game.subwayworkerserver.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.apache.tomcat.util.http.fileupload.FileUploadBase.CONTENT_TYPE;

@Component
public class TLSClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse<Map<String, Object>> sendPostRequest(
            String fullUrl,
            Object requestBody,
            String contentType,
            SSLContext sslContext
    ) throws Exception {
        return executeRequest(fullUrl, "POST", contentType, requestBody, null, sslContext);
    }

    public ApiResponse<Map<String, Object>> sendPostRequest(
            String fullUrl,
            Object requestBody,
            String contentType,
            Map<String, String> headers,
            SSLContext sslContext
    ) throws Exception {
        return executeRequest(fullUrl, "POST", contentType, requestBody, headers, sslContext);
    }

    public ApiResponse<Map<String, Object>> sendGetRequest(
            String fullUrl,
            Map<String, String> headers,
            SSLContext sslContext
    ) throws Exception {
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

            InputStream stream = (statusCode >= 200 && statusCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                responseBody = reader.lines().collect(Collectors.joining("\n"));
            }

            if (statusCode != 200) {
                // 토스 API가 에러 시에도 JSON을 반환한다고 가정하고 에러를 BaseException으로 변환
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "외부 API 오류: " + responseBody);
            }

            Map<String, Object> data = objectMapper.readValue(responseBody, new TypeReference<>() {});

            return new ApiResponse<>(statusCode, "성공", data);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public SSLContext createSSLContext(String certPem, String keyPem) throws Exception {

        X509Certificate cert = loadCertificate(certPem);
        PrivateKey key = loadPrivateKey(keyPem);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        char[] password = "".toCharArray(); // 개인 키에 암호가 없다고 가정
        keyStore.setCertificateEntry("client-cert", cert);
        keyStore.setKeyEntry("client-key", key, password, new X509Certificate[]{cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null); // TrustManager는 기본값 사용

        return context;
    }

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