package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.dto.response.coworker.CoworkerListResponse;
import com.game.subwayworkerserver.dto.response.item.CaptureItemListResponse;
import com.game.subwayworkerserver.dto.response.item.TrapItemListResponse;
import com.game.subwayworkerserver.dto.response.skill.SkillByUserResponse;
import com.game.subwayworkerserver.dto.response.user.UserResponse;
import com.game.subwayworkerserver.dto.response.user.UuidResponse;
import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.dto.request.user.UserCreateRequest;
import com.game.subwayworkerserver.profanity.ProfanityCheck;
import com.game.subwayworkerserver.service.skill.SkillCardService;
import com.game.subwayworkerserver.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@Tag(name = "User API", description = "사용자 정보 및 프로필 관리 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SkillCardService skillCardService;

    @Operation(summary = "사용자 이름 중복 체크", description = "유저이름 중복 체크")
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyUser(@ProfanityCheck @RequestParam("name") String name) {
        return ResponseEntity.ok(userService.verifyUserName(name));
    }

    @Operation(summary = "최초 사용자 등록", description = "유저이름과 유저가 선택한 동료 속성을 받아 저장합니다.")
    @PostMapping("/user")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "home 에서 사용되는 사용자 정보 조회", description = "익명 ID (UUID)를 기반으로 사용자 기본 정보를 조회합니다.")
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserInfo(@CurrentUuid String uuid) {
        return ResponseEntity.ok(userService.getUserInfos(uuid));
    }

    @Operation(summary = "포획도구리스트 조회", description = "유저 uuid 로 사용 가능한 포획도구 리스트 조회")
    @GetMapping("/user/capture-item-list")
    public ResponseEntity<CaptureItemListResponse> getCaptureListByUser(@CurrentUuid String uuid) {
        return ResponseEntity.ok(userService.getCaptureItemList(uuid));
    }

    @Operation(summary = "트랩 도구리스트 조회", description = "유저 uuid 로 사용 가능한 트랩 리스트 조회")
    @GetMapping("/user/trap-item-list")
    public ResponseEntity<TrapItemListResponse> getTrapListByUser(@CurrentUuid String uuid) {
        return ResponseEntity.ok(userService.getTrapItemList(uuid));
    }

    @Operation(summary = "동료 리스트 조회", description = "동료 리스트를 조회")
    @GetMapping("/user/coworkers")
    public ResponseEntity<CoworkerListResponse> getCoworkersListByUser(@CurrentUuid String uuid) {
        return ResponseEntity.ok(userService.getCoworkerListByUser(uuid));
    }

    @Operation(summary = "유저 삭제", description = "유저 삭제 기능")
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteUser(@CurrentUuid String uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "유저가 가지고 있는 스킬 카드 전체 조회", description = "유저의 모든 스킬 리스트를 조회")
    @GetMapping("/user/skillCards")
    public ResponseEntity<SkillByUserResponse> getUserSkillCards(@CurrentUuid String uuid) {
        return ResponseEntity.ok(skillCardService.getUserSkillCards(uuid));
    }

    @Operation(summary = "토스 해시로 uuid 반환받기")
    @GetMapping("/user/toss")
    public ResponseEntity<UuidResponse> getUUIDByHash(@RequestParam("hash") String tossHash) {
        log.debug("tossHash = {}", tossHash);
        return ResponseEntity.ok(userService.getUuidByHash(tossHash));
    }

}
