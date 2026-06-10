package com.game.subwayworkerserver.toss.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!dev")
@Component
public class TossSecretsLoader {

    @Value("${TOSS_CLIENT_CERT_PEM}")
    private String clientCertPem;

    @Value("${TOSS_CLIENT_KEY_PEM}")
    private String clientKeyPem;

    public record MtlsCredentials(String clientCertPem, String clientKeyPem) {}

    public MtlsCredentials getMtlsCredentials() {
        return new MtlsCredentials(clientCertPem, clientKeyPem);
    }
}
