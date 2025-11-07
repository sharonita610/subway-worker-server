package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.request.SkillByUserRequest;
import com.game.subwayworkerserver.controller.request.SkillCardConfigureRequest;
import com.game.subwayworkerserver.controller.request.UpgradeRequest;
import com.game.subwayworkerserver.controller.response.*;
import com.game.subwayworkerserver.service.CoworkerService;
import com.game.subwayworkerserver.service.SkillCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Coworker API", description = "동료 관련 API")
@RestController
@RequiredArgsConstructor
public class CoworkerController {

    private final CoworkerService coworkerService;
    private final SkillCardService skillCardService;

    @Operation(
            summary = "유저 생성시 사용할 동료 속성 리스트 [완]",
            description = "최초로 함께할 동료의 속성 정보를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "동료의 속성 리스트를 조회")
            }
    )
    @GetMapping("/coworker-types")
    public ResponseEntity<CoworkerTypesResponse> getCoworkerElements() {
        return ResponseEntity.ok(coworkerService.getCoworkerElements());
    }

    @Operation(
            summary = "동료를 파트너로 지정하기 [완]",
            description = "동료의 id 리스트를 통해 파트너로 지정한다."
    )
    @PostMapping("/coworker/partners")
    public ResponseEntity<HttpStatus> addCoworkerAsAPartner(@Valid @RequestBody PartnerConfigureRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        coworkerService.configurePartners(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "스킬 리스트 조회 [완]",
            description = "스킬 리스트 조회 현재 선택 된 동료 id 가 포함 된 리스트"
    )
    @GetMapping("/coworker/skills")
    public ResponseEntity<SkillByCoworkerResponse> getSkillListByUserAndCoworker(@RequestBody SkillByUserRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardService.getSkillListByUserAndCoworker(uuid, request));
    }

    @Operation(
            summary = "강화 가능한 스킬 카드 리스트 조회 [완]",
            description = "강화가 가능한 동일한 카드만 조회하는 기능"
    )
    @GetMapping("/skills/upgrade")
    public ResponseEntity<SkillListBySkillIdResponse> getSkillsAvailableToUpgrade(@RequestParam long skillCardId) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardService.getSkillsAvailableToUpgrade(uuid, skillCardId));
    }

    @Operation(
            summary = "카드 강화하기 [완] ",
            description = "강화가 가능한 동일한 카드만 조회하는 기능"
    )
    @PostMapping("/skills/upgrade")
    public ResponseEntity<UpgradedSkillCardResponse> upgradeCard(@RequestBody UpgradeRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardService.upgradeCard(uuid, request));
    }

    @Operation(
            summary = "스킬카드를 파트너에게 장착 또는 제거하기 [완]",
            description = "스킬카드를 파트너에게 장착한다."
    )
    @PostMapping("/coworker/skill-card")
    public ResponseEntity<HttpStatus> addSkillCardToCoworker(@Valid @RequestBody SkillCardConfigureRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        skillCardService.addSkillCardToCoworker(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "유저 강화하기 [개발 중] ",
            description = "유저를 강화하기 위한 요청을 한다."
    )
    @PostMapping("/coworker/upgrade")
    public ResponseEntity<UserUpgradeResponse> upgradeCoworker(@Valid @RequestBody UpgradeRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(coworkerService.upgradeCoworker(uuid, request));
    }
}
