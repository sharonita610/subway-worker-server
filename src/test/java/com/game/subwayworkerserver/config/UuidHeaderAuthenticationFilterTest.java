package com.game.subwayworkerserver.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UuidHeaderAuthenticationFilterTest {

    private final UuidHeaderAuthenticationFilter filter = new UuidHeaderAuthenticationFilter();

    private final AtomicReference<Authentication> authDuringChain = new AtomicReference<>();
    private final AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    private final FilterChain chain = (req, res) -> {
        authDuringChain.set(SecurityContextHolder.getContext().getAuthentication());
        mdcDuringChain.set(MDC.get(UuidHeaderAuthenticationFilter.MDC_USER_ID_KEY));
    };

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void run(String headerValue) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/store");
        if (headerValue != null) {
            request.addHeader(UuidHeaderAuthenticationFilter.HEADER_USER_ID, headerValue);
        }
        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    @Test
    void 헤더가_있으면_uuid가_principal이_되고_MDC에_기록된다() throws Exception {
        run("user-uuid-1");

        assertThat(authDuringChain.get()).isNotNull();
        assertThat(authDuringChain.get().isAuthenticated()).isTrue();
        assertThat(authDuringChain.get().getPrincipal()).isEqualTo("user-uuid-1");
        assertThat(mdcDuringChain.get()).isEqualTo("user-uuid-1");
    }

    @Test
    void 헤더가_없으면_인증을_설정하지_않고_GUEST로_기록한다() throws Exception {
        run(null);

        assertThat(authDuringChain.get()).isNull();
        assertThat(mdcDuringChain.get()).isEqualTo("GUEST");
    }

    @Test
    void 공백_헤더는_없는_것으로_본다() throws Exception {
        run("   ");

        assertThat(authDuringChain.get()).isNull();
        assertThat(mdcDuringChain.get()).isEqualTo("GUEST");
    }

    @Test
    void 요청이_끝나면_MDC를_비운다() throws Exception {
        run("user-uuid-1");

        assertThat(MDC.get(UuidHeaderAuthenticationFilter.MDC_USER_ID_KEY)).isNull();
    }
}
