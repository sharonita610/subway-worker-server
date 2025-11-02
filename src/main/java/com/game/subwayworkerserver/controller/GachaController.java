package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.service.GachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GachaController {

    private final GachaService gachaService;

    @GetMapping("/lucky-gacha")
    public ResponseEntity<LuckyGachaListResponse> luckyGacha() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(gachaService.getGachaList(uuid));
    }

}
