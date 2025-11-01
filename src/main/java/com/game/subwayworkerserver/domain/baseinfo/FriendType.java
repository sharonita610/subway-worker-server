package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.util.CharacterGenerator;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 캐릭터의 스탯 프로필(직업)을 정의하는 Enum.
 * 각 상수는 해당 직업의 초기 스탯 범위를 관리합니다.
 */
@Getter
public enum FriendType {

    MOONGSHIL("뭉실", new CharacterGenerator.Range(58, 91), new CharacterGenerator.Range(0, 5), new CharacterGenerator.Range(23, 36), 10, new CharacterGenerator.Range(14, 21), new CharacterGenerator.Range(3, 7)),
    MOKA("모카", new CharacterGenerator.Range(44, 83), new CharacterGenerator.Range(0, 5), new CharacterGenerator.Range(23, 36), 10, new CharacterGenerator.Range(16, 23), new CharacterGenerator.Range(8, 15)),
    LOONY("루니", new CharacterGenerator.Range(31, 54), new CharacterGenerator.Range(8, 25), new CharacterGenerator.Range(40, 78), 13, new CharacterGenerator.Range(6, 19), new CharacterGenerator.Range(1, 3)),
    POOBY("푸비", new CharacterGenerator.Range(35, 58), new CharacterGenerator.Range(0, 10), new CharacterGenerator.Range(27, 44), 10, new CharacterGenerator.Range(19, 31), new CharacterGenerator.Range(3, 7)),
    YOMY("요미", new CharacterGenerator.Range(43, 78), new CharacterGenerator.Range(3, 15), new CharacterGenerator.Range(33, 57), 11, new CharacterGenerator.Range(16, 26), new CharacterGenerator.Range(4, 11));

    private final String koreanName;
    private final CharacterGenerator.Range hpRange;
    private final CharacterGenerator.Range mpNowRange;
    private final CharacterGenerator.Range mpMaxRange;
    private final int manaRecovery;
    private final CharacterGenerator.Range attackRange;
    private final CharacterGenerator.Range defenseRange;

    FriendType(String koreanName, CharacterGenerator.Range hpRange, CharacterGenerator.Range mpNowRange, CharacterGenerator.Range mpMaxRange, int manaRecovery, CharacterGenerator.Range attackRange, CharacterGenerator.Range defenseRange) {
        this.koreanName = koreanName;
        this.hpRange = hpRange;
        this.mpNowRange = mpNowRange;
        this.mpMaxRange = mpMaxRange;
        this.manaRecovery = manaRecovery;
        this.attackRange = attackRange;
        this.defenseRange = defenseRange;
    }

    public static FriendType fromName(final String type) {
        for (FriendType friendType : FriendType.values()) {
            if (friendType.koreanName.equalsIgnoreCase(type)) {
                return friendType;
            }
        }
        throw new BaseException(ErrorCode.BAD_REQUEST, "유효하지 않은 FriendType 직업 이름입니다: " + type);
    }

    public static FriendType getRandomAttribute() {
        FriendType[] attributes = FriendType.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(attributes.length);
        return attributes[randomIndex];
    }

    public static List<FriendType> getAll() {
        return List.of(FriendType.values());
    }
}
