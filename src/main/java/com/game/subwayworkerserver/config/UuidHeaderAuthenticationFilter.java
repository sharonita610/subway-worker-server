package com.game.subwayworkerserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청 헤더 "X-User-ID"에서 UUID를 추출하여 Security Context에 저장하는 필터.
 */
public class UuidHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uuid = request.getHeader(HEADER_USER_ID);

        // 1. UUID가 존재하고, 현재 Security Context에 인증 정보가 없을 경우에만 처리
        if (uuid != null && !uuid.trim().isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {

            // UUID를 Principal로 사용하는 Authentication 객체 생성
            // Principal: UUID (String), Credentials: null, Authorities: null (익명/기본 권한)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uuid, null, null);

            // 요청 상세 정보 추가
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 2. Security Context에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("UUID 인증 성공: " + uuid + "이 Security Context에 저장되었습니다.");

        }

        filterChain.doFilter(request, response);
    }
}
