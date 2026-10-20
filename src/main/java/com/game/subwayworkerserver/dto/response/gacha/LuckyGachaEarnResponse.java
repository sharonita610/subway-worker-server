package com.game.subwayworkerserver.dto.response.gacha;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;

import java.util.ArrayList;
import java.util.List;

public record LuckyGachaEarnResponse(
        List<DefaultRewardInfo> defaultRewardList,
        ExtraRewardInfo extraRewardList
) {
    public static LuckyGachaEarnResponse of(
            List<DefaultGift> defaultReward,
            List<ExtraGift> extraReward
    ) {
        return new LuckyGachaEarnResponse(
                defaultReward.stream()
                        .map(DefaultRewardInfo::from)
                        .toList(),
                ExtraRewardInfo.from(extraReward)
        );
    }

    private record DefaultRewardInfo(String name, int quantity) {
        public static DefaultRewardInfo from(final DefaultGift defaultGift) {
            return new DefaultRewardInfo(defaultGift.name(), defaultGift.quantity());
        }
    }

    private record ExtraRewardInfo(
            List<TreasureRewardInfo> treasureList,
            List<TreasureRewardInfo> itemList
    ) {
        public static ExtraRewardInfo from(final List<ExtraGift> extraGiftList) {

            List<TreasureRewardInfo> treasureList = new ArrayList<>();
            List<TreasureRewardInfo> itemsList = new ArrayList<>();

            for (ExtraGift extraGift : extraGiftList) {
                String giftName = extraGift.name();
                int quantity = extraGift.quantity();

                if (GachaTreasure.isGacha(giftName)) {
                    treasureList.add(new TreasureRewardInfo(giftName, quantity));
                } else {
                    itemsList.add(new TreasureRewardInfo(giftName, quantity));
                }
            }

            return new ExtraRewardInfo(treasureList, itemsList);
        }
    }

    private record TreasureRewardInfo(String name, int quantity) {

    }
}
