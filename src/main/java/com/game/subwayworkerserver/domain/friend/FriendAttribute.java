package com.game.subwayworkerserver.domain.friend;

public enum FriendAttribute {

    /** 1. 일(성과) - 목표 달성, 성과, 추진력 */
    MUNGSIL("일(성과)"),

    /** 2. 인간관계(협력) - 동료애, 팀워크, 네트워크 */
    MOCCA("인간관계(협력)"),

    /** 3. 체력(건강) - 피로, 체력 관리, 에너지 */
    RUNI("체력(건강)"),

    /** 4. 마음(멘탈) - 스트레스 관리, 동기 부여 */
    PUBI("마음(멘탈)"),

    /** 5. 지식(성장) - 학습, 정보, 전문성 */
    YOMI("지식(성장)");

    private final String description;

    FriendAttribute(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
