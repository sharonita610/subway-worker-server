package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.UserEntity;

public record UserBaseInfo(
        long id,
        String username,
        int coin,
        int stageLevel,
        int ticketCount

) {
    public static UserBaseInfo of(UserEntity user) {
        return new UserBaseInfo(
                user.getId(),
                user.getName(),
                user.getCoin(),
                user.getStage(),
                user.getTicket()
        );
    }
}
