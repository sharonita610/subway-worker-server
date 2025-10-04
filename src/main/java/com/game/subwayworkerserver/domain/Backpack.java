package com.game.subwayworkerserver.domain;

import com.game.subwayworkerserver.domain.item.CaptureItem;
import com.game.subwayworkerserver.domain.item.TrapItem;

import java.util.List;

public record Backpack(List<TrapItem> trapItemList, List<CaptureItem> captureItemList) {

}
