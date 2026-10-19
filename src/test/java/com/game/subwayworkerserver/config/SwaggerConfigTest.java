package com.game.subwayworkerserver.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    private Operation operationWith200() {
        Operation operation = new Operation();
        operation.setResponses(new ApiResponses().addApiResponse("200", new io.swagger.v3.oas.models.responses.ApiResponse()));
        return operation;
    }

    @Test
    void 보호_경로에는_401_응답이_붙는다() {
        Operation operation = operationWith200();

        SwaggerConfig.decorate("/store", PathItem.HttpMethod.GET, operation);

        assertThat(operation.getResponses()).containsKey("401");
        assertThat(operation.getSecurity()).isNull();
    }

    @Test
    void 공개_경로는_인증_요구를_빼고_401도_붙이지_않는다() {
        Operation operation = operationWith200();

        SwaggerConfig.decorate("/user", PathItem.HttpMethod.POST, operation);

        assertThat(operation.getResponses()).doesNotContainKey("401");
        assertThat(operation.getSecurity()).isEmpty();
    }

    @Test
    void 공개_여부는_메서드까지_본다() {
        assertThat(PublicEndpoints.isPublic(org.springframework.http.HttpMethod.GET, "/user")).isFalse();
        assertThat(PublicEndpoints.isPublic(org.springframework.http.HttpMethod.POST, "/user")).isTrue();
        assertThat(PublicEndpoints.isPublic(org.springframework.http.HttpMethod.DELETE, "/auth/anything/here")).isTrue();
        assertThat(PublicEndpoints.isPublic(org.springframework.http.HttpMethod.GET, "/stage/coworkers")).isFalse();
    }
}
