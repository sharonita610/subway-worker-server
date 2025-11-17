package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.CaptureItemListResponse;
import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.*;
import com.game.subwayworkerserver.profanity.ProfanityCheck;
import com.game.subwayworkerserver.service.SkillCardService;
import com.game.subwayworkerserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "User API", description = "사용자 정보 및 프로필 관리 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SkillCardService skillCardService;

    @Operation(
            summary = "사용자 이름 중복 체크 [완]",
            description = "유저이름 중복 체크",
            responses = {
                    @ApiResponse(responseCode = "200", description = "boolean")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyUser(@ProfanityCheck @RequestParam("name") String name) {
        return ResponseEntity.ok(userService.verifyUserName(name));
    }

    @Operation(
            summary = "최초 사용자 등록 [완]",
            description = "유저이름과 유저가 선택한 동료 속성을 받아 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록 성공")
            }
    )
    @PostMapping("/user")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(
            summary = "home 에서 사용되는 사용자 정보 조회 [완]",
            description = "익명 ID (UUID)를 기반으로 사용자 기본 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적인 정보 조회"),
                    @ApiResponse(responseCode = "400", description = "필수 헤더(X-User-ID) 누락 또는 유효하지 않은 ID",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserInfo(
    ) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(userService.getUserInfos(uuid));
    }

    @Operation(
            summary = "포획도구리스트 조회 [완]",
            description = "유저 uuid 로 사용 가능한 포획도구 리스트 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포획도구 리스트")
            }
    )
    @GetMapping("/user/capture-item-list")
    public ResponseEntity<CaptureItemListResponse> getCaptureListByUser() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(userService.getCaptureItemList(uuid));
    }

    @Operation(
            summary = "트랩 도구리스트 조회 [완]",
            description = "유저 uuid 로 사용 가능한 트랩 리스트 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "트랩 리스트")
            }
    )
    @GetMapping("/user/trap-item-list")
    public ResponseEntity<TrapItemListResponse> getTrapListByUser() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(userService.getTrapItemList(uuid));
    }

    @Operation(
            summary = "동료 리스트 조회 [완]",
            description = "동료 리스트를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "동료 리스트")
            }
    )
    @GetMapping("/user/coworkers")
    public ResponseEntity<CoworkerListResponse> getCoworkersListByUser() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(userService.getCoworkerListByUser(uuid));
    }

    @Operation(
            summary = "유저 삭제 [완]",
            description = "유저 삭제 기능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저 삭제 완료")
            }
    )
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteUser() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        userService.deleteUser(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "유저가 가지고 있는 스킬 카드 전체 조회 [완]",
            description = "유저의 모든 스킬 리스트를 조회"
    )
    @GetMapping("/user/skillCards")
    public ResponseEntity<SkillByUserResponse> getUserSkillCards() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardService.getUserSkillCards(uuid));
    }
}
