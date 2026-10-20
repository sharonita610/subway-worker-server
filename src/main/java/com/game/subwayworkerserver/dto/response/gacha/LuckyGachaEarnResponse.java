package com.game.subwayworkerserver.dto.response.gacha;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import com.game.subwayworkerserver.dto.response.common.RewardInfo;

import java.util.ArrayList;
import java.util.List;

public record LuckyGachaEarnResponse(
        List<RewardInfo> defaultRewardList,
        ExtraRewardInfo extraRewardList
) {
    public static LuckyGachaEarnResponse of(List<DefaultGift> defaultReward, List<ExtraGift> extraReward) {
        return new LuckyGachaEarnResponse(
                defaultReward.stream().map(gift -> RewardInfo.of(gift.name(), gift.quantity())).toList(),
                ExtraRewardInfo.from(extraReward)
        );
    }

    /** 추가 보상은 보물과 아이템으로 나눠 보여준다. */
    private record ExtraRewardInfo(List<RewardInfo> treasureList, List<RewardInfo> itemList) {
        public static ExtraRewardInfo from(List<ExtraGift> extraGiftList) {
            List<RewardInfo> treasureList = new ArrayList<>();
            List<RewardInfo> itemsList = new ArrayList<>();
            for (ExtraGift extraGift : extraGiftList) {
                RewardInfo info = RewardInfo.of(extraGift.name(), extraGift.quantity());
                if (GachaTreasure.isGacha(extraGift.name())) {
                    treasureList.add(info);
                } else {
                    itemsList.add(info);
                }
            }
            return new ExtraRewardInfo(treasureList, itemsList);
        }
    }
}
