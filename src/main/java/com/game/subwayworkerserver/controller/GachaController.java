package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.request.OpenSkillCardRequest;
import com.game.subwayworkerserver.controller.response.EarnedSkillCardResponse;
import com.game.subwayworkerserver.controller.response.LuckyGachaEarnResponse;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.service.GachaService;
import com.game.subwayworkerserver.service.SkillCardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GachaController {

    private final GachaService gachaService;
    private final SkillCardService skillCardService;

    @Operation(
            summary = "가챠 리스트 조회 기능 [완]",
            description = "사용자의 가챠 리스트와 티켓을 조회하는 기능"
    )
    @GetMapping("/lucky-gacha")
    public ResponseEntity<LuckyGachaListResponse> luckyGacha() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(gachaService.getGachaList(uuid));
    }

    @Operation(
            summary = "가챠 뽑기 [완]",
            description = "사용자의 가챠 리스트와 티켓을 조회하는 기능"
    )
    @PostMapping("/lucky-gacha")
    public ResponseEntity<LuckyGachaEarnResponse> openLuckyGacha(@RequestBody OpenLuckyGachaRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(gachaService.openLuckyGacha(uuid, request));
    }

    @Operation(
            summary = "원하는 타입의 스킬 카드 뽑기 [완]",
            description = "스킬 카드 뽑기"
    )
    @PostMapping("/lucky-skill-card")
    public ResponseEntity<EarnedSkillCardResponse> getRandomSkillCardByType(@RequestBody OpenSkillCardRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardService.getRandomSkillCardByType(uuid, request));
    }

}
