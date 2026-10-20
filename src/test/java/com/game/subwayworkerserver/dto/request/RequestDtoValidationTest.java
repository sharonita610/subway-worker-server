package com.game.subwayworkerserver.dto.request;

import com.game.subwayworkerserver.dto.request.coworker.PartnerConfigureRequest;
import com.game.subwayworkerserver.dto.request.coworker.SkillCardConfigureRequest;
import com.game.subwayworkerserver.dto.request.gps.GpsRequest;
import com.game.subwayworkerserver.dto.request.market.SellingRequest;
import com.game.subwayworkerserver.dto.request.system.SystemMailRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** 요청 DTO의 Bean Validation 규칙을 고정한다. (@ProfanityCheck는 Spring 빈이 필요해 여기서는 다루지 않는다) */
class RequestDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static List<String> paths(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).sorted().toList();
    }

    @Test
    void GpsRequest는_위도_경도_범위와_최소_반경을_검사한다() {
        assertThat(validator.validate(new GpsRequest(37.5, 127.0, 1.0))).isEmpty();
        assertThat(paths(validator.validate(new GpsRequest(91.0, 181.0, 0.1)))).containsExactly("latitude", "longitude", "rangeKm");
        assertThat(paths(validator.validate(new GpsRequest(null, 127.0, 1.0)))).containsExactly("latitude");
    }

    @Test
    void SystemMailRequest는_제목과_내용이_비어_있으면_거절한다() {
        assertThat(validator.validate(new SystemMailRequest("제목", "내용", List.of()))).isEmpty();
        assertThat(paths(validator.validate(new SystemMailRequest(" ", "", null)))).containsExactly("content", "title");
    }

    @Test
    void PartnerConfigureRequest는_파트너를_1명에서_3명까지만_받는다() {
        assertThat(validator.validate(new PartnerConfigureRequest(List.of(1L, 2L)))).isEmpty();
        assertThat(paths(validator.validate(new PartnerConfigureRequest(List.of())))).containsExactly("coworkerIds");
        assertThat(paths(validator.validate(new PartnerConfigureRequest(List.of(1L, 2L, 3L, 4L))))).containsExactly("coworkerIds");
        assertThat(paths(validator.validate(new PartnerConfigureRequest(null)))).containsExactly("coworkerIds");
    }

    @Test
    void SkillCardConfigureRequest는_카드_타입이_필수다() {
        assertThat(validator.validate(new SkillCardConfigureRequest(1L, "ACTIVE", 10L))).isEmpty();
        assertThat(paths(validator.validate(new SkillCardConfigureRequest(1L, null, 10L)))).containsExactly("skillCardType");
    }

    @Test
    void 검증_규칙이_없는_요청은_어떤_값이든_통과한다() {
        assertThat(validator.validate(new SellingRequest(0L, -1, -1, -1))).isEmpty();
    }
}
