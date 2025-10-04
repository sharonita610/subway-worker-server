package com.game.subwayworkerserver.domain.market;

import com.game.subwayworkerserver.domain.item.TrapItem;
import com.game.subwayworkerserver.domain.Treasure;

import java.util.List;

public record ItemMarket (
        List<TrapItem> trapItemList,
        // 골드리스트
        List<Treasure> treasureList
){
}
