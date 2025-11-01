package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.response.FriendTypesResponse;
import com.game.subwayworkerserver.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Friend API", description = "동료 관련 API")
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(
            summary = "유저 생성시 사용할 동료 속성 리스트 [완]",
            description = "최초로 함께할 동료의 속성 정보를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "동료의 속성 리스트를 조회")
            }
    )
    @GetMapping("/friend-types")
    public ResponseEntity<FriendTypesResponse> getFriendAttributes() {
        return ResponseEntity.ok(friendService.getFriendAttributes());
    }
}
