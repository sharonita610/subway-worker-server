package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.controller.request.gacha.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.request.gacha.OpenSkillCardRequest;
import com.game.subwayworkerserver.controller.response.skill.EarnedSkillCardResponse;
import com.game.subwayworkerserver.controller.response.gacha.LuckyGachaEarnResponse;
import com.game.subwayworkerserver.controller.response.gacha.LuckyGachaListResponse;
import com.game.subwayworkerserver.service.gacha.GachaService;
import com.game.subwayworkerserver.service.skill.SkillCardService;
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
    public ResponseEntity<LuckyGachaListResponse> luckyGacha(@CurrentUuid String uuid) {
        return ResponseEntity.ok(gachaService.getGachaList(uuid));
    }

    @Operation(
            summary = "가챠 뽑기 [완]",
            description = "사용자의 가챠 리스트와 티켓을 조회하는 기능"
    )
    @PostMapping("/lucky-gacha")
    public ResponseEntity<LuckyGachaEarnResponse> openLuckyGacha(@CurrentUuid String uuid, @RequestBody OpenLuckyGachaRequest request) {
        return ResponseEntity.ok(gachaService.openLuckyGacha(uuid, request));
    }

    @Operation(
            summary = "원하는 타입의 스킬 카드 뽑기 [완]",
            description = "스킬 카드 뽑기"
    )
    @PostMapping("/lucky-skill-card")
    public ResponseEntity<EarnedSkillCardResponse> getRandomSkillCardByType(@CurrentUuid String uuid, @RequestBody OpenSkillCardRequest request) {
        return ResponseEntity.ok(skillCardService.getRandomSkillCardByType(uuid, request));
    }

}
