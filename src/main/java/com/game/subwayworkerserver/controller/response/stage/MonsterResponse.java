package com.game.subwayworkerserver.controller.response.stage;

import com.game.subwayworkerserver.domain.stage.MonsterSampleData;

import java.util.List;

public record MonsterResponse(List<MonsterSampleData.MonsterType> monsters) {
}
