package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.response.LevelUpRewardResponse;
import com.game.subwayworkerserver.controller.response.UserMailBoxResponse;
import com.game.subwayworkerserver.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @Operation(
            summary = "우편함 조회 [test]",
            description = "동료 리스트를 조회"
    )
    @GetMapping("/mail-box")
    public ResponseEntity<UserMailBoxResponse> mailBox() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getMailBox(uuid));
    }

    @Operation(
            summary = "단일 보상 받기",
            description = "mailId 로 단일 보상을 받는 기능"
    )
    @PostMapping("/mail-box")
    public String claimRewardById(@RequestParam("id") long mailId) {
        String uuid = SecurityUtils.getCurrentUserUuid();

        return null;
    }


    @Operation(
            summary = "전체 단일 보상 받기",
            description = "우편함에 있는 모든 보상 받기"
    )
    @PostMapping("/mail-box/all")
    public String claimAllRewards() {
        String uuid = SecurityUtils.getCurrentUserUuid();

        return null;
    }

    @Operation(
            summary = "레벨 업 보상 받기 [test]",
            description = "사용자의 레벨 상승 보상받기"
    )
    @GetMapping("/level-up-reward")
    public ResponseEntity<LevelUpRewardResponse> getLevelUpReward() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getLevelUpReward(uuid));
    }

}
