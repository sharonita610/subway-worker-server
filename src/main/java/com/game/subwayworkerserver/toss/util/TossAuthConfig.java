package com.game.subwayworkerserver.toss.util;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "toss.auth")
@Getter
public class TossAuthConfig {

    private String secretName;
    private String aadString;
    private String decryptionKeyBase64;
    private String authApiBase;
    private Integer port;

    // API 호출 시 편리하도록 헬퍼 메서드를 추가할 수 있습니다.
    public String getAuthApiUrl(String endpointPath) {
        // authApiBase + endpointPath
        return "https://apps-in-toss-api.toss.im/api-partner"+ endpointPath;
    }
}
