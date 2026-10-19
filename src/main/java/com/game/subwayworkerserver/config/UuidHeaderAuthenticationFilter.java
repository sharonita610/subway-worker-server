package com.game.subwayworkerserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * X-User-ID 헤더의 UUID를 현재 요청의 principal로 등록하고, 로그 MDC에 user_id로 남긴다.
 * 헤더가 없으면 인증을 설정하지 않으므로 보호된 경로는 SecurityConfig에서 401로 거절된다.
 */
public class UuidHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String MDC_USER_ID_KEY = "user_id";
    static final String MDC_GUEST = "GUEST";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uuid = request.getHeader(HEADER_USER_ID);
        boolean hasUuid = uuid != null && !uuid.isBlank();

        try {
            MDC.put(MDC_USER_ID_KEY, hasUuid ? uuid : MDC_GUEST);

            if (hasUuid && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uuid, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USER_ID_KEY);
        }
    }
}
