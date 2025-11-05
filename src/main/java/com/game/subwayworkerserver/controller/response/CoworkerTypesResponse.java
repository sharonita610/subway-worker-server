package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;

import java.util.List;

public record CoworkerTypesResponse(
        List<CoworkerTypeInfo> typeNames
) {
    public static CoworkerTypesResponse from(final List<CoworkerType> allAttributesAsList) {
        return new CoworkerTypesResponse(
                allAttributesAsList.stream()
                        .map(coworker ->
                                CoworkerTypeInfo.of(coworker.name(), coworker.getKoreanName())
                        ).toList()
        );
    }

    private record CoworkerTypeInfo(String engName, String korName){
        public static CoworkerTypeInfo of(String engName, String korName) {
            return new CoworkerTypeInfo(engName, korName);
        }
    }
}
