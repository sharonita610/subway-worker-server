package com.game.subwayworkerserver.dto.response.user;

import java.util.List;

public record UserResponse(
     UserBaseInfo baseInfo,
     UserLevelInfo levelInfo,
     List<CoworkerInUseInfo> coworkerInUseInfoList
) {
}
