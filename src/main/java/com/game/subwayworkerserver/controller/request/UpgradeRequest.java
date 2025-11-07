package com.game.subwayworkerserver.controller.request;

import java.util.List;

public record UpgradeRequest(
        long targetId,
        List<Long> materialIds,
        int price
){
}
