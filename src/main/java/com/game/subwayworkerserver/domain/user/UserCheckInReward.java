package com.game.subwayworkerserver.domain.user;

import com.game.subwayworkerserver.entity.RewardType;
import com.game.subwayworkerserver.service.CheckInReward;

import java.util.ArrayList;
import java.util.List;

public record UserCheckInReward(
        int day,
        String korName,
        boolean alreadyClaimed,
        List<DailyRewardInfo> rewardList
) {

    public static List<UserCheckInReward> toDomain(int claimedRewardDayUpTo, List<CheckInReward> allRewards) {

        return allRewards.stream()
                .map(reward -> {
                    final int rewardDay = reward.getDay();
                    boolean claimed = rewardDay <= claimedRewardDayUpTo;
                    List<DailyRewardInfo> rewardInfos = createRewardList(reward);
                    return new UserCheckInReward(
                            rewardDay,
                            String.format("%d일차", rewardDay), // 한국어 이름은 'N일차'로 임시 설정
                            claimed,
                            rewardInfos
                    );
                })
                .toList();
    }

    private static List<DailyRewardInfo> createRewardList(CheckInReward reward) {

        List<DailyRewardInfo> list = new ArrayList<>();

        if (reward.getCoin() > 0) {
            list.add(new DailyRewardInfo(RewardType.COIN.name(), (int) reward.getCoin()));
        }
        if (reward.getVacationTicket() > 0) {
            list.add(new DailyRewardInfo(RewardType.VACATION_TICKET.name(), (int) reward.getVacationTicket()));
        }
        if (reward.getGachaTicket() > 0) {
            list.add(new DailyRewardInfo(RewardType.GACHA_TICKET.name(), (int) reward.getGachaTicket()));
        }
        if (reward.getMaterialFriend() != null) {
            list.add(new DailyRewardInfo(reward.getMaterialFriend().name(), 1));
        }
        if (reward.getNoAds() != null) {
            list.add(new DailyRewardInfo(reward.getNoAds().name(), 1));
        }

        return list;
    }

    private record DailyRewardInfo(
            String rewardName,
            int quantity
    ) {
    }
}