package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.controller.response.MonsterResponse;
import com.game.subwayworkerserver.controller.response.StageFriendListResponse;
import com.game.subwayworkerserver.controller.sampleData.MonsterSampleData;
import com.game.subwayworkerserver.service.StageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stage API", description = "스테이지 관련 API")
@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @GetMapping("/stage/friends")
    public ResponseEntity<StageFriendListResponse>  getStageFriends(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stage/monsters")
    public ResponseEntity<MonsterResponse> getStageMonsters(@RequestParam("num") int num){
        if(num<=0){
            return ResponseEntity.badRequest().build();
        }
        List<MonsterSampleData.MonsterType> stageMonsters = stageService.getStageMonsters(num);

        if(stageMonsters != null){
            return ResponseEntity.ok(new MonsterResponse(stageMonsters));
        }
        return ResponseEntity.badRequest().build();
    }
}
