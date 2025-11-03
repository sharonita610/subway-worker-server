package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.DefaultGift;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.ExtraGift;

import java.util.ArrayList;
import java.util.List;

public record LuckyGachaEarnResponse(
        List<DefaultRewardInfo> defaultRewardList,
        ExtraRewardInfo extraReward
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
            return new DefaultRewardInfo(defaultGift.type(), defaultGift.quantity());
        }
    }

    private record ExtraRewardInfo(
            TreasureRewardInfo treasure,
            List<TreasureRewardInfo> items
    ) {
        public static ExtraRewardInfo from(final List<ExtraGift> extraGiftList) {

            TreasureRewardInfo singleTreasure = null;
            List<TreasureRewardInfo> itemsList = new ArrayList<>();

            for (ExtraGift extraGift : extraGiftList) {
                String giftName = extraGift.type();
                int quantity = extraGift.quantity();

                if (GachaTreasure.isGacha(giftName)) {
                    TreasureRewardInfo currentTreasure = new TreasureRewardInfo(giftName, quantity);
                    if (singleTreasure == null) {
                        singleTreasure = currentTreasure;
                    } else {
                        itemsList.add(currentTreasure);
                    }
                } else {
                    itemsList.add(new TreasureRewardInfo(giftName, quantity));
                }
            }

            return new ExtraRewardInfo(singleTreasure, itemsList);
        }


    }

    private record TreasureRewardInfo(String name, int quantity) {

    }
}
