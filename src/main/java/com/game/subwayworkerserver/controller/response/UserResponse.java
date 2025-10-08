package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record UserResponse(
     UserBaseInfo baseInfo,
     UserLevelInfo levelInfo,
     TreasureInfo treasureInfo,
     List<FriendInUseInfo> friendInUseInfoList
) {
}
