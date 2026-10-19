package com.game.subwayworkerserver.controller.response.gps;

import com.game.subwayworkerserver.domain.reward.RandomBoxReward;
import com.game.subwayworkerserver.domain.item.ItemName;

import java.util.List;

public record GpsRandomGiftResponse(
        List<DefaultGiftInfo> randomGiftList,
        List<BonusGiftInfo> bonusGiftInfo
) {
    public static GpsRandomGiftResponse of(
            final List<RandomBoxReward.DefaultGift> defaultGifts,
            final List<RandomBoxReward.ExtraGift> bonusGifts
    ) {
        List<DefaultGiftInfo> defaultGiftInfos = defaultGifts.stream()
                .map(defaultGift ->
                        new DefaultGiftInfo(defaultGift.name(), defaultGift.koreanName(), defaultGift.quantity())
                ).toList();
        List<BonusGiftInfo> bonusGiftInfos = bonusGifts.stream()
                .map(bonusGift ->
                        BonusGiftInfo.of(bonusGift.name(), bonusGift.quantity())
                ).toList();
        return new  GpsRandomGiftResponse(defaultGiftInfos, bonusGiftInfos);
    }

    private record DefaultGiftInfo(
        String name,
        String koreanName,
        int quantity
    ) {
    }

    private record BonusGiftInfo(
            String name,
            String koreanName,
            String description,
            int quantity
    ) {

        public static BonusGiftInfo of(String itemConstantName, int quantity) {
            if (itemConstantName.equals("GACHA_TICKET")) {
                return new BonusGiftInfo("GACHA_TICKET", "가챠티켓", "가챠티켓", quantity);
            }
            ItemName itemName = ItemName.fromConstantName(itemConstantName);
            return new BonusGiftInfo(itemName.toString(), itemName.getName(), itemName.getDescription(), quantity);
        }
    }
}
