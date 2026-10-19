package com.game.subwayworkerserver.config;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

/**
 * X-User-ID 없이 접근할 수 있는 경로. SecurityConfig의 허용 규칙과 API 문서의 401 표시가 이 목록 하나를 본다.
 */
public final class PublicEndpoints {

    public static final String[] GET = {"/user/toss", "/logo", "/stage/monsters", "/coworker-types"};
    public static final String[] POST = {"/user", "/verify", "/system/mail"};
    public static final String[] ANY = {
            "/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**"
    };

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private PublicEndpoints() {
    }

    public static boolean isPublic(HttpMethod method, String path) {
        return matches(ANY, path)
                || (HttpMethod.GET.equals(method) && matches(GET, path))
                || (HttpMethod.POST.equals(method) && matches(POST, path));
    }

    private static boolean matches(String[] patterns, String path) {
        return Arrays.stream(patterns).anyMatch(pattern -> MATCHER.match(pattern, path));
    }
}
