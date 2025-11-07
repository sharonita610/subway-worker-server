package com.game.subwayworkerserver.service;

public enum SkillSortType {

    PRICE_ASC("가격오름차순"),
    PRICE_DESC("가격내림차순"),
    LEVEL_DESC("레벨높은순"),
    DEFAULT("가격내림차순");

    private final String koreanName;

    SkillSortType(String koreanName) {
        this.koreanName = koreanName;
    }

    public static SkillSortType fromKoreanName(String name) {
        for (SkillSortType type : values()) {
            if (type.koreanName.equals(name)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
