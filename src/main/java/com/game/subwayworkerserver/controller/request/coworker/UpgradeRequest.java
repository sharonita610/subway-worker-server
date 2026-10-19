package com.game.subwayworkerserver.controller.request.coworker;

import java.util.List;

public record UpgradeRequest(
        long targetId,
        List<Long> materialIds,
        int price
){
}
