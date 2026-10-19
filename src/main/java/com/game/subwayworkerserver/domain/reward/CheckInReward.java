package com.game.subwayworkerserver.domain.reward;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum CheckInReward {

    DAY_1(1, 30000L, 0L, 0L, null, null),
    DAY_2(2, 0L, 50L, 0L, null, null),
    DAY_3(3, 50000L, 0L, 0L, null, null),
    DAY_4(4, 0L, 100L, 0L, null, null),
    DAY_5(5, 50000L, 0L, 0L, MaterialFriend.ENERZ_LV2, null),
    DAY_6(6, 100000L, 0L, 2L, null, null),
    DAY_7(7, 0L, 100L, 0L, MaterialFriend.ENERZ_LV4, NoAds.NO_ADS_3DAYS);

    private final int day;
    private final long coin;
    private final long vacationTicket;
    private final long gachaTicket;
    private final MaterialFriend materialFriend;
    private final NoAds noAds;

    CheckInReward(int day, long coin, long vacationTicket, long gachaTicket, MaterialFriend materialFriend, NoAds noAds) {
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
        int actualDay = (day > 7) ? ((day - 1) % 7) + 1 : day;

        CheckInReward reward = DAY_MAP.get(actualDay);

        if (reward == null) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "출석 보상 정의에 오류가 있습니다: " + actualDay);
        }
        return reward;
    }

    public static List<CheckInReward> getAllRewards() {
        return Arrays.asList(CheckInReward.values());
    }

    @Getter
    public enum MaterialFriend {
        ENERZ_LV2(CoworkerType.ENERZ, 2),
        ENERZ_LV3(CoworkerType.ENERZ, 3),
        ENERZ_LV4(CoworkerType.ENERZ, 4),
        ENERZ_LV5(CoworkerType.ENERZ, 5);

        private final CoworkerType type;
        private final int level;

        MaterialFriend(CoworkerType type, int level) {
            this.type = type;
            this.level = level;
        }
    }

    @Getter
    public enum NoAds {
        NO_ADS_1DAY(1),
        NO_ADS_3DAYS(3),
        NO_ADS_15DAYS(15),
        NO_ADS_30DAYS(30);

        private final int days;

        NoAds(int days) {
            this.days = days;
        }
    }
}