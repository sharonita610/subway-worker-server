package com.game.subwayworkerserver.config;

import org.springdoc.core.utils.SpringDocUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    static {
        // @CurrentUuid 매개변수는 헤더에서 주입되므로 API 파라미터로 노출하지 않는다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUuid.class);
    }
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("API Test") // API의 제목
                .description("Let's practice Swagger UI") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }
}
