package com.game.subwayworkerserver.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 매개변수에 현재 요청 사용자의 UUID를 주입한다.
 * UuidHeaderAuthenticationFilter가 X-User-ID 헤더 값을 principal(String)로 넣어 두면
 * Spring Security의 @AuthenticationPrincipal 해석기가 그 값을 꺼내 준다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUuid {
}
