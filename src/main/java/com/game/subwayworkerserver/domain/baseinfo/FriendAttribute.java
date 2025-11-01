package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
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

    public static FriendAttribute fromKoreanName(String koreanName) {
        for (FriendAttribute attribute : FriendAttribute.values()) {
            if (attribute.koreanName.equalsIgnoreCase(koreanName)) {
                return attribute;
            }
        }
        throw new BaseException(ErrorCode.INVALID_FRIEND_ATTRIBUTE);
    }

    public static List<FriendAttribute> getAllAttributesAsList() {
        FriendAttribute[] attributeArray = FriendAttribute.values();
        return List.of(attributeArray);
    }

    public static FriendAttribute getRandomAttribute() {
        FriendAttribute[] attributes = FriendAttribute.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(attributes.length);
        return attributes[randomIndex];
    }
}
