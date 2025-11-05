package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.response.CoworkerListResponse;
import com.game.subwayworkerserver.controller.response.CoworkerTypesResponse;
import com.game.subwayworkerserver.controller.response.SkillListResponse;
import com.game.subwayworkerserver.service.CoworkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Coworker API", description = "동료 관련 API")
@RestController
@RequiredArgsConstructor
public class CoworkerController {

    private final CoworkerService coworkerService;

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
            summary = "스킬 리스트 조회",
            description = "스킬 리스트 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "트랩 리스트")
            }
    )
    @GetMapping("/coworker/skills")
    public ResponseEntity<SkillListResponse> getSkillListByUser( ) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok().build();
    }

}
