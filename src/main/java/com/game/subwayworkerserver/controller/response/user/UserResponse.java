package com.game.subwayworkerserver.controller.response.user;

import java.util.List;

public record UserResponse(
     UserBaseInfo baseInfo,
     UserLevelInfo levelInfo,
     List<CoworkerInUseInfo> coworkerInUseInfoList
) {
}
