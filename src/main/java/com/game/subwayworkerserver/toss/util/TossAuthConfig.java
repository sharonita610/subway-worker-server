package com.game.subwayworkerserver.toss.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = ("spring.toss.auth"))
@Getter
@Setter
public class TossAuthConfig {

    private String secretName;
    private String aadString;
    private String decryptionKeyBase64;
    private String authApiBase;
    private Integer port;

    // API 호출 시 편리하도록 헬퍼 메서드를 추가할 수 있습니다.
    public String getAuthApiUrl(String endpointPath) {
        log.debug("authApiBase = {}", authApiBase);
        return "https://apps-in-toss-api.toss.im/api-partner"+ endpointPath;
    }
}
