package com.game.subwayworkerserver.config;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security Context에서 사용자 정보를 추출하는 유틸리티 클래스.
 * 정적 메서드를 통해 애플리케이션의 어느 계층에서든 UUID를 조회할 수 있게 합니다.
 */
public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 UUID를 가져옵니다.
     * (가정: UuidHeaderAuthenticationFilter에서 Principal을 String 타입의 UUID로 설정했습니다.)
     *
     * @return 현재 인증된 사용자의 UUID (String)
     * @throws IllegalStateException 인증 정보가 Security Context에 없을 경우 (인증 실패)
     */
    public static String getCurrentUserUuid() {
        // 1. Security Context로부터 Authentication 객체를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 객체가 유효한지 확인합니다.
        // isAuthenticated()는 SecurityConfig 설정에 따라 AnonymousUser까지 포함할 수 있으나,
        // 여기서는 인증 필터가 넣어준 Principal을 확인하는 것에 초점을 맞춥니다.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Security Context에 인증된 사용자 정보가 없습니다.");
        }

        // 3. Principal 객체에서 UUID를 추출 (String 타입으로 캐스팅)
        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return (String) principal;
        }

        // 예상치 못한 Principal 타입일 경우 예외 처리
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "Principal 객체가 예상한 String (UUID) 타입이 아닙니다. 실제 타입: " + principal.getClass().getName());
    }
}
