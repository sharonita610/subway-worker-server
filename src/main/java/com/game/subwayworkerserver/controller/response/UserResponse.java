package com.game.subwayworkerserver.controller.response;

import java.util.List;

public record UserResponse(
     UserBaseInfo baseInfo,
     UserLevelInfo levelInfo,
     BagpackInfo bagpackInfo,
     TreasureInfo treasureInfo,
     List<FriendInUseInfo> friendInUseInfoList
) {
}
