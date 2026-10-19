package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.controller.request.stage.DungeonRequest;
import com.game.subwayworkerserver.controller.request.stage.StageClearRequest;
import com.game.subwayworkerserver.controller.response.stage.DungeonResponse;
import com.game.subwayworkerserver.controller.response.stage.MonsterResponse;
import com.game.subwayworkerserver.controller.response.stage.StageClearResponse;
import com.game.subwayworkerserver.controller.response.coworker.StageCoworkerListResponse;
import com.game.subwayworkerserver.domain.stage.StageMonsterData;
import com.game.subwayworkerserver.service.coworker.CoworkerService;
import com.game.subwayworkerserver.service.stage.StageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stage API", description = "스테이지 관련 API")
@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;
    private final CoworkerService coworkerService;

    @Operation(
            summary = "스테이지에서 사용할 동료들의 능력 리스트 조회기능",
            description = "유저가 장착중인 동료, 그리고 그 동료의 능력을 조회"
    )
    @GetMapping("/stage/coworkers")
    public ResponseEntity<StageCoworkerListResponse> getStageCoworkers(@CurrentUuid String uuid) {
        return ResponseEntity.ok(coworkerService.getUsingCoworkers(uuid));
    }

    @Operation(
            summary = "스테이지 몬스터 정보 읽어오기",
            description = "파라미터 num 1 or 2"
    )
    @GetMapping("/stage/monsters")
    public ResponseEntity<MonsterResponse> getStageMonsters(@RequestParam("num") int num) {
        if (num <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<StageMonsterData.MonsterType> stageMonsters = stageService.getStageMonsters(num);

        if (stageMonsters != null) {
            return ResponseEntity.ok(new MonsterResponse(stageMonsters));
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(
            summary = "사용자의 스테이지 기록하고 보상 받기",
            description = "완료한 스테이지를 기록하고 보상을 받는 기능"
    )
    @PostMapping("/stage/record")
    public ResponseEntity<StageClearResponse> recordStage(@CurrentUuid String uuid, @RequestBody StageClearRequest request) {
        return ResponseEntity.ok(stageService.recordStage(uuid, request));
    }

    @Operation(
            summary = "사용자의 던전을 기록하고 보상 받기",
            description = "사용자가 방문한 던전을 기록하고 보상을 받는 기능"
    )
    @PostMapping("/dungeon/record")
    public ResponseEntity<StageClearResponse> recordDungeon(@CurrentUuid String uuid, @RequestBody DungeonRequest request) {
        return ResponseEntity.ok(stageService.recordDungeon(uuid, request));
    }

    @Operation(
            summary = "사용자의 던전 참여 가능 후 uuid 를 조회",
            description = "사용자가 8시간 안에 방문한적이 있는지 조회하고 없으면 던전 uuid 받기"
    )
    @GetMapping("/dungeon/record")
    public ResponseEntity<DungeonResponse> checkDungeon(@CurrentUuid String uuid, @RequestParam("stationName") String stationName) {
        return ResponseEntity.ok(stageService.checkDungeon(uuid, stationName));
    }

}
