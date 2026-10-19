package com.game.subwayworkerserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing(BaseTimeEntity의 생성·수정 시각)을 켠다.
 * 애플리케이션 클래스가 아닌 별도 설정에 두어 JPA 없이 뜨는 슬라이스 테스트(@WebMvcTest)에 영향을 주지 않는다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
