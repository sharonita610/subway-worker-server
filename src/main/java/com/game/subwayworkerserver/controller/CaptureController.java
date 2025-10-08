package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.request.CaptureFriendRequest;
import com.game.subwayworkerserver.controller.request.CaptureItemListResponse;
import com.game.subwayworkerserver.controller.response.CaptureResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Capture API", description = "포획 관련 API")
@RestController
@RequiredArgsConstructor
public class CaptureController {

    @Operation(
            summary = "포획도구리스트 조회",
            description = "유저 uuid 로 사용 가능한 포획도구 리스트 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포획도구 리스트")
            }
    )
    @GetMapping("/capture-items")
    public ResponseEntity<CaptureItemListResponse> getCaptureListByUser(
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "동료 포획 요청",
            description = "포획도구로 동료를 포획 시도한다, 포획 도구에 따른 확률로 성공 유무를 가린다." +
                    "포획 성공 시 gps 에서 해당 동료는 없어져야 한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포획 결과에 따른 보상")
            }
    )
    @PostMapping("/capture-friend")
    public ResponseEntity<CaptureResponse> captureFriend(@RequestBody CaptureFriendRequest request) {
        return ResponseEntity.ok().build();
    }


}
