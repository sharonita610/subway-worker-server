package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.response.CoworkerTypesResponse;
import com.game.subwayworkerserver.service.CoworkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
