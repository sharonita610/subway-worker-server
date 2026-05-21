package com.game.subwayworkerserver.controller.response.user;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.UserEntity;

import java.util.List;

public record UserBaseInfo(
        long id,
        String username,
        String gender,
        int coin,
        int stageLevel,
        int vacationTicket,
        int gachaTicket,
        boolean hasPendingReward,
        boolean hasTrappedSomething,
        boolean hasDailyCheckInReward,
        boolean hasPendingMail,
        int consecutiveAttendanceDays,
        boolean freeAdsUser,
        List<String> badgeName
) {
    public static UserBaseInfo of(UserEntity user, List<SubscriptionItem> items) {

        return new UserBaseInfo(
                user.getId(),
                user.getName(),
                user.getGender().name().toLowerCase(),
                user.getCoin(),
                user.getStage(),
                user.getVacationTicket(),
                user.getGachaTicket(),
                user.isHasPendingReward(),
                user.isTrapSuccess(),
                user.isHasDailyCheckInReward(),
                user.isHasPendingMail(),
                user.getConsecutiveAttendanceDays(),
                user.isAdFreeActive(),
                items.stream().map(Enum::name).toList()
        );
    }
}
