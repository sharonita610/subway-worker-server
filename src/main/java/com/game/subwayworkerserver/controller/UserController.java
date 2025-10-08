package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 정보 및 프로필 관리 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    @Operation(
            summary = "사용자 이름 중복 체크",
            description = "유저이름 중복 체크",
            responses = {
                    @ApiResponse(responseCode = "200", description = "boolean")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyUser(@RequestParam("name") String name) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "최초 사용자 등록",
            description = "유저이름과 유저가 선택한 동료 속성을 받아 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록 성공")
            }
    )
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "home 에서 사용되는 사용자 정보 조회",
            description = "익명 ID (UUID)를 기반으로 사용자 기본 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적인 정보 조회"),
                    @ApiResponse(responseCode = "400", description = "필수 헤더(X-User-ID) 누락 또는 유효하지 않은 ID",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserInfo(
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok().build();
    }


}
