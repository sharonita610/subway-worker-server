package com.game.subwayworkerserver.dto.response.stage;

import com.game.subwayworkerserver.domain.stage.StageMonsterData;

import java.util.List;

public record MonsterResponse(List<StageMonsterData.MonsterType> monsters) {
}
