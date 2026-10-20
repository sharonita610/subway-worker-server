package com.game.subwayworkerserver.dto.response.coworker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;

import java.util.List;

/**
 * 동료를 식별하는 공통 정보. 동료가 나오는 모든 응답이 이 record를 품어서 같은 키 이름을 쓴다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoworkerSummary(
        Long id,
        String engName,
        String korName,
        String position,
        Integer level,
        String engElement,
        String korElement
) {
    public static CoworkerSummary of(CoworkerEntity coworker) {
        return new CoworkerSummary(
                coworker.getId(),
                coworker.getCoworkerType().name(),
                coworker.getName(),
                coworker.getLevel().getTitle(),
                coworker.getLevel().getLevel(),
                coworker.getCoworkerElement().name(),
                coworker.getCoworkerElement().getKoreanName()
        );
    }

    public static CoworkerSummary of(CoworkerOnSale coworker) {
        return new CoworkerSummary(
                coworker.coworkerId(), coworker.engName(), coworker.korName(), coworker.position(),
                coworker.level(), coworker.engElement(), coworker.korElement()
        );
    }

    public static List<CoworkerSummary> of(List<CoworkerEntity> coworkers) {
        return coworkers.stream().map(CoworkerSummary::of).toList();
    }
}
