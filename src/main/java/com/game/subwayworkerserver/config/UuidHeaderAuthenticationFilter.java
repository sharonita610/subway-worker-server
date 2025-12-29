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

public class UuidHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String MDC_UUID_KEY = "request_id"; // 로그 포맷에서 쓸 키값

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uuid = request.getHeader(HEADER_USER_ID);

        try {
            if (uuid != null && !uuid.trim().isEmpty()) {
                MDC.put(MDC_UUID_KEY, uuid);
            } else {
                MDC.put(MDC_UUID_KEY, "GUEST"); // ID가 없을 경우 대비
            }
            MDC.put("request_uri", request.getRequestURI());

            if (uuid != null && !uuid.trim().isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uuid, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}
