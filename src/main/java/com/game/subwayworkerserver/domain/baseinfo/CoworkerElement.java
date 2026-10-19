package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum CoworkerElement {

    PERFORMANCE("성과"),
    COOPERATION("협력"),
    HEALTH("건강"),
    MENTAL("멘탈"),
    KNOWLEDGE("지식");

    private final String koreanName;

    CoworkerElement(String koreanName) {
        this.koreanName = koreanName;
    }

    public static CoworkerElement getRandomAttribute() {
        CoworkerElement[] attributes = CoworkerElement.values();

        int randomIndex = ThreadLocalRandom.current().nextInt(attributes.length);

        return attributes[randomIndex];
    }
}
