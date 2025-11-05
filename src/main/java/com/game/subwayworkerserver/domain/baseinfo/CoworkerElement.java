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
        // 1. 모든 Enum 상수 배열을 가져옵니다.
        CoworkerElement[] attributes = CoworkerElement.values();

        // 2. 배열의 길이 범위 내에서 무작위 인덱스를 생성합니다.
        int randomIndex = ThreadLocalRandom.current().nextInt(attributes.length);

        // 3. 해당 인덱스의 상수를 반환합니다.
        return attributes[randomIndex];
    }
}
