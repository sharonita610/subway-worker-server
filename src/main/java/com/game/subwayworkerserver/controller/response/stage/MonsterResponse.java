package com.game.subwayworkerserver.controller.response.stage;

import com.game.subwayworkerserver.domain.stage.StageMonsterData;

import java.util.List;

public record MonsterResponse(List<StageMonsterData.MonsterType> monsters) {
}
