package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.UserEntity;

public record UserBaseInfo(
        long id,
        String username,
        String gender,
        int coin,
        int stageLevel,
        int vacationTicket,
        int gotchaTicket,
        boolean hasPendingReward,
        boolean hasTrappedSomething

) {
    public static UserBaseInfo of(UserEntity user) {
        return new UserBaseInfo(
                user.getId(),
                user.getName(),
                user.getGender().name().toLowerCase(),
                user.getCoin(),
                user.getStage(),
                user.getVacationTicket(),
                user.getGotchaTicket(),
                user.isHasPendingReward(),
                user.isTrapSuccess()
        );
    }
}
