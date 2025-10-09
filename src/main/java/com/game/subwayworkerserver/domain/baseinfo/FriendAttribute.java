package com.game.subwayworkerserver.domain.baseinfo;

/**
 * 캐릭터의 고유 이름 속성 (사용자가 선택하거나 부여받는 5가지 분류)
 */
public enum FriendAttribute {

    MOONGSHIL("뭉실"),
    MOKA("모카"),
    LOONY("루니"),
    POOBIE("푸비"),
    YOMY("요미");

    private final String koreanName; // 사용자가 입력할 수 있는 한글 이름

    FriendAttribute(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static FriendAttribute fromKoreanName(String koreanName) {
        for (FriendAttribute attribute : FriendAttribute.values()) {
            if (attribute.koreanName.equalsIgnoreCase(koreanName)) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("Invalid FriendAttribute name: " + koreanName + ". Must be one of: 뭉실, 모카, 루니, 푸비, 요미");
    }
}
