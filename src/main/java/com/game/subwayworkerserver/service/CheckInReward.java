package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum CheckInReward {

    DAY_1(1, 30000L, 0L, 0L, 0, 0),
    DAY_2(2, 0L, 50L, 0L, 0, 0),
    DAY_3(3, 50000L, 0L, 0L, 0, 0),
    DAY_4(4, 0L, 100L, 0L, 0, 0),
    DAY_5(5, 50000L, 0L, 0L, 1, 0),
    DAY_6(6, 100000L, 0L, 2L, 0, 0),
    DAY_7(7, 0L, 100L, 0L, 1, 3);

    private final int day;
    private final long coin;
    private final long vacationTicket;
    private final long gachaTicket;
    private final int materialFriend;
    private final int noAds;

    CheckInReward(int day, long coin, long vacationTicket, long gachaTicket, int materialFriend, int noAds) {
        this.day = day;
        this.coin = coin;
        this.vacationTicket = vacationTicket;
        this.gachaTicket = gachaTicket;
        this.materialFriend = materialFriend;
        this.noAds = noAds;
    }

    private static final Map<Integer, CheckInReward> DAY_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(c -> c.day, c -> c));

    public static CheckInReward getRewardByDay(int day) {
        if (day <= 0) {
            day = 1;
        }

        // 7일차를 초과하면, 7로 나눈 나머지에 따라 순환 (1~7)
        int actualDay = (day > 7) ? ((day - 1) % 7) + 1 : day;

        // 미리 생성된 맵에서 조회 (null 체크는 필요하지 않습니다.)
        CheckInReward reward = DAY_MAP.get(actualDay);

        // Map에 키가 1~7로 모두 존재하므로, 이 예외는 이론상 발생하지 않으며,
        // 런타임 오류 방지를 위해 Enum에 없는 키가 사용되었을 때만 처리합니다.
        if (reward == null) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "출석 보상 정의에 오류가 있습니다: " + actualDay);
        }
        return reward;
    }
}