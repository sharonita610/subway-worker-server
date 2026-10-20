package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.dto.response.coworker.CoworkerTypesResponse;
import com.game.subwayworkerserver.dto.response.coworker.UserUpgradeResponse;
import com.game.subwayworkerserver.dto.response.skill.SkillByCoworkerResponse;
import com.game.subwayworkerserver.dto.response.skill.SkillListBySkillIdResponse;
import com.game.subwayworkerserver.dto.response.skill.UpgradedSkillCardResponse;
import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.dto.request.coworker.PartnerConfigureRequest;
import com.game.subwayworkerserver.dto.request.coworker.SkillCardConfigureRequest;
import com.game.subwayworkerserver.dto.request.coworker.UpgradeRequest;
import com.game.subwayworkerserver.service.coworker.CoworkerService;
import com.game.subwayworkerserver.service.skill.SkillCardService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "유저 생성시 사용할 동료 속성 리스트", description = "최초로 함께할 동료의 속성 정보를 조회")
    @GetMapping("/coworker-types")
    public ResponseEntity<CoworkerTypesResponse> getCoworkerElements() {
        return ResponseEntity.ok(coworkerService.getCoworkerElements());
    }

    @Operation(summary = "동료를 파트너로 지정하기", description = "동료의 id 리스트를 통해 파트너로 지정한다.")
    @PostMapping("/coworker/partners")
    public ResponseEntity<HttpStatus> addCoworkerAsAPartner(@CurrentUuid String uuid, @Valid @RequestBody PartnerConfigureRequest request) {
        coworkerService.configurePartners(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "스킬 리스트 조회", description = "스킬 리스트 조회 현재 선택 된 동료 id 가 포함 된 리스트")
    @GetMapping("/coworker/skills")
    public ResponseEntity<SkillByCoworkerResponse> getSkillListByUserAndCoworker(@CurrentUuid String uuid,
            @RequestParam("id") long coworkerId, @RequestParam("type")  String type
    ) {
        return ResponseEntity.ok(skillCardService.getSkillListByUserAndCoworker(uuid, coworkerId, type));
    }

    @Operation(summary = "강화 가능한 스킬 카드 리스트 조회", description = "강화가 가능한 동일한 카드만 조회하는 기능")
    @GetMapping("/skills/upgrade")
    public ResponseEntity<SkillListBySkillIdResponse> getSkillsAvailableToUpgrade(@CurrentUuid String uuid, @RequestParam("id") long skillCardId) {
        return ResponseEntity.ok(skillCardService.getSkillsAvailableToUpgrade(uuid, skillCardId));
    }

    @Operation(summary = "카드 강화하기", description = "강화가 가능한 동일한 카드만 조회하는 기능")
    @PostMapping("/skills/upgrade")
    public ResponseEntity<UpgradedSkillCardResponse> upgradeCard(@CurrentUuid String uuid, @RequestBody UpgradeRequest request) {
        return ResponseEntity.ok(skillCardService.upgradeCard(uuid, request));
    }

    @Operation(summary = "스킬카드를 파트너에게 장착 또는 제거하기", description = "스킬카드를 파트너에게 장착한다.")
    @PostMapping("/coworker/skill-card")
    public ResponseEntity<HttpStatus> addSkillCardToCoworker(@CurrentUuid String uuid, @Valid @RequestBody SkillCardConfigureRequest request) {
        skillCardService.addSkillCardToCoworker(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "유저 강화하기", description = "유저를 강화하기 위한 요청을 한다.")
    @PostMapping("/coworker/upgrade")
    public ResponseEntity<UserUpgradeResponse> upgradeCoworker(@CurrentUuid String uuid, @Valid @RequestBody UpgradeRequest request) {
        return ResponseEntity.ok(coworkerService.upgradeCoworker(uuid, request));
    }
}
