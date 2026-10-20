package com.game.subwayworkerserver.dto.request.coworker;

import java.util.List;

public record UpgradeRequest(
        long targetId,
        List<Long> materialIds,
        int price
){
}
