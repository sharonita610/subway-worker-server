package com.game.subwayworkerserver.controller.response.stage;

import com.game.subwayworkerserver.controller.sampleData.MonsterSampleData;

import java.util.List;

public record MonsterResponse(List<MonsterSampleData.MonsterType> monsters) {
}
