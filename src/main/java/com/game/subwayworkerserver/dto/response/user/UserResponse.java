package com.game.subwayworkerserver.dto.response.user;

import com.game.subwayworkerserver.dto.response.coworker.CoworkerSummary;
import java.util.List;

public record UserResponse(
     UserBaseInfo baseInfo,
     UserLevelInfo levelInfo,
     List<CoworkerSummary> coworkerInUseInfoList
) {
}
