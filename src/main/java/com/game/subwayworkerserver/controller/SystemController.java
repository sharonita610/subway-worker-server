package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.SystemMailRequest;
import com.game.subwayworkerserver.controller.response.user.CheckInListResponse;
import com.game.subwayworkerserver.controller.response.user.LevelUpRewardResponse;
import com.game.subwayworkerserver.controller.response.subscription.SubscribeListResponse;
import com.game.subwayworkerserver.controller.response.user.UserMailBoxResponse;
import com.game.subwayworkerserver.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final ResourceLoader resourceLoader;

    @Operation(
            summary = "우편함 조회 [완]",
            description = "동료 리스트를 조회"
    )
    @GetMapping("/mail-box")
    public ResponseEntity<UserMailBoxResponse> mailBox() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getMailBox(uuid));
    }

    @Operation(
            summary = "단일 보상 받기 [완]",
            description = "mailId 로 단일 보상을 받는 기능"
    )
    @PostMapping("/mail-box")
    public ResponseEntity<HttpStatus> claimRewardById(@RequestParam("id") long mailId) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        systemService.claimRewardById(uuid, mailId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(
            summary = "전체 보상 받기 [완]",
            description = "우편함에 있는 모든 보상 받기"
    )
    @PostMapping("/mail-box/all")
    public ResponseEntity<HttpStatus> claimAllRewards() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        systemService.claimAllRewards(uuid);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(
            summary = "레벨 업 보상 받기 [완]",
            description = "사용자의 레벨 상승 보상받기"
    )
    @GetMapping("/level-up-reward")
    public ResponseEntity<LevelUpRewardResponse> getLevelUpReward() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getLevelUpReward(uuid));
    }

    @Operation(
            summary = "출근부 조회 [완]",
            description = "연속 출석부 조회 및 현재까지 받은 보상조회"
    )
    @GetMapping("/check-in-list")
    public ResponseEntity<CheckInListResponse> getCheckInRewardList() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getCheckInRewardList(uuid));
    }

    @Operation(
            summary = "출석 보상 받기 [완]",
            description = "아직 받지 못한 출석 보상 받기"
    )
    @PostMapping("/check-in")
    public ResponseEntity<HttpStatus> claimCheckInReward() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        systemService.claimCheckInReward(uuid);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(
            summary = "구독 리스트 조회 [완]",
            description = "현재 구독 상태가 포함된 구독 리스트 조회"
    )
    @GetMapping("/subscribe-list")
    public ResponseEntity<SubscribeListResponse> getSubscribeList() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(systemService.getSubscribeList(uuid));
    }

    @Operation(
            summary = "구독하기 [완]",
            description = "구독하기"
    )
    @PostMapping("/subscribe")
    public ResponseEntity<HttpStatus> subscribe(@RequestParam("id") long subscribeId) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        systemService.subscribe(uuid, subscribeId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(
            summary = "logo 조회 api [완]",
            description = "로고를 조회하는 api"
    )
    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo() {
        Resource resource = resourceLoader.getResource("classpath:/static/logo.png");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @Operation(
            summary = "system 메일을 보내는 기능 [완]",
            description = "사용자에게 system 메일을 보상과 함께 제공하는 기능"
    )
    @PostMapping("/system/mail")
    public ResponseEntity<HttpStatus> sendSystemMail(@Valid @RequestBody SystemMailRequest request) {
        systemService.sendSystemMail(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
