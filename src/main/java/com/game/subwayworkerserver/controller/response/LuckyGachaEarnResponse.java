package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.DefaultGift;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.ExtraGift;

import java.util.List;

public record LuckyGachaEarnResponse(
        List<TreasureRewardInfo> treasureList,
        List<DefaultRewardInfo> defaultRewardList,
        List<ExtraRewardInfo> extraRewardList
) {
    public static LuckyGachaEarnResponse of(
            List<GachaTreasure> randomGacha,
            List<DefaultGift> defaultReward,
            List<ExtraGift> extraReward
    ) {
        return new LuckyGachaEarnResponse(
                randomGacha.stream()
                        .map(TreasureRewardInfo::from)
                        .toList(),
                defaultReward.stream()
                        .map(DefaultRewardInfo::from)
                        .toList(),
                extraReward.stream()
                        .map(ExtraRewardInfo::from)
                        .toList()
        );
    }

    private record TreasureRewardInfo(String name, int quantity) {
        public static TreasureRewardInfo from(final GachaTreasure gachaTreasure) {
            return new TreasureRewardInfo(gachaTreasure.getItemName(), 1);
        }
    }

    private record DefaultRewardInfo(String name, int quantity) {
        public static DefaultRewardInfo from(final DefaultGift defaultGift) {
            return new DefaultRewardInfo(defaultGift.type(), defaultGift.quantity());
        }
    }

    private record ExtraRewardInfo(String name, int quantity) {
        public static ExtraRewardInfo from(final ExtraGift extraGift) {
            return new ExtraRewardInfo(extraGift.type(), extraGift.quantity());
        }
    }
}
