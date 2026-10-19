package com.game.subwayworkerserver.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE, FEMALE;

    @JsonCreator
    public static Gender fromString(final String value) {
        return Gender.valueOf(value.toUpperCase());
    }
}
