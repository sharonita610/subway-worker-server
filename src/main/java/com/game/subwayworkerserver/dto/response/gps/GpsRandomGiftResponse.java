package com.game.subwayworkerserver.dto.response.gps;

import com.game.subwayworkerserver.domain.reward.RandomBoxReward;
import com.game.subwayworkerserver.dto.response.common.RewardInfo;

import java.util.List;

public record GpsRandomGiftResponse(
        List<RewardInfo> randomGiftList,
        List<RewardInfo> bonusGiftInfo
) {
    public static GpsRandomGiftResponse of(List<RandomBoxReward.DefaultGift> defaultGifts, List<RandomBoxReward.ExtraGift> bonusGifts) {
        return new GpsRandomGiftResponse(
                defaultGifts.stream().map(gift -> RewardInfo.of(gift.name(), gift.koreanName(), gift.quantity())).toList(),
                bonusGifts.stream().map(gift -> RewardInfo.ofItem(gift.name(), gift.quantity())).toList()
        );
    }
}
