package com.game.subwayworkerserver.config;

import com.game.subwayworkerserver.exception.ErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class SwaggerConfig {

    static final String USER_ID_SCHEME = "X-User-ID";
    static final String ERROR_RESPONSE_SCHEMA = "ErrorResponse";
    private static final String UNAUTHORIZED = "401";

    static {
        // @CurrentUuid 매개변수는 헤더에서 주입되므로 API 파라미터로 노출하지 않는다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUuid.class);
    }

    @Bean
    public OpenAPI openAPI() {
        ResolvedSchema errorSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class));
        Components components = new Components()
                .addSecuritySchemes(USER_ID_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name(UuidHeaderAuthenticationFilter.HEADER_USER_ID)
                        .description("회원 생성 또는 토스 로그인으로 받은 사용자 UUID"));
        if (errorSchema != null && errorSchema.schema != null) {
            components.addSchemas(ERROR_RESPONSE_SCHEMA, errorSchema.schema);
        }
        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(USER_ID_SCHEME))
                .info(new Info()
                        .title("Subway Worker Server API")
                        .description("출퇴근길 위치 기반으로 동료를 포획·성장시키는 게임 서버 API. "
                                + "공개 경로를 제외한 모든 요청은 X-User-ID 헤더가 필요하다.")
                        .version("1.0.0"));
    }

    /** 공개 경로는 자물쇠를 빼고, 보호 경로에는 401 응답을 일괄로 붙인다. */
    @Bean
    public OpenApiCustomizer authenticationDocsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((httpMethod, operation) ->
                            decorate(path, httpMethod, operation)));
        };
    }

    static void decorate(String path, PathItem.HttpMethod httpMethod, Operation operation) {
        if (PublicEndpoints.isPublic(HttpMethod.valueOf(httpMethod.name()), path)) {
            operation.setSecurity(List.of());
            return;
        }
        if (operation.getResponses() != null && operation.getResponses().containsKey(UNAUTHORIZED)) {
            return;
        }
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/" + ERROR_RESPONSE_SCHEMA);
        operation.getResponses().addApiResponse(UNAUTHORIZED, new ApiResponse()
                .description("X-User-ID 헤더 누락")
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(ref))));
    }

}
