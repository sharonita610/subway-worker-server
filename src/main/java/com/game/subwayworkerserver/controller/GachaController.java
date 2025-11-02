package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.service.GachaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GachaController {

    private final GachaService gachaService;

    @Operation(
            summary = "가챠 리스트 조회 기능 [완]",
            description = "사용자의 가챠 리스트와 티켓을 조회하는 기능"
    )
    @GetMapping("/lucky-gacha")
    public ResponseEntity<LuckyGachaListResponse> luckyGacha() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(gachaService.getGachaList(uuid));
    }

}
