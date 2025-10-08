package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.request.PurchaseRequest;
import com.game.subwayworkerserver.controller.response.RandomCaptureItemResponse;
import com.game.subwayworkerserver.controller.response.StoreItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Store API", description = "상점 관련 API")
@RestController
@RequiredArgsConstructor
public class StoreController {

    @Operation(
            summary = "store 조회",
            description = "store에서 구매 가능한 모든 아이템의 리스트를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적인 정보 조회")
            }
    )
    @GetMapping("/store")
    public ResponseEntity<StoreItemResponse> getStoreItems() {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "트랩 구매",
            description = "구매하고자 하는 트랩 정보를 받아 저장",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    @PostMapping("/buy/trap-item")
    public ResponseEntity<Void> buyTrapItem(@RequestBody PurchaseRequest request) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "포획도구 구매",
            description = "구매하고자 하는 포획도구 정보를 받아 저장",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "400", description = "코인이 부족합니다.")
            }
    )
    @PostMapping("/buy/capture-item")
    public ResponseEntity<Void> buyCaptureItem(@RequestBody PurchaseRequest request) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "포획도구 랜덤 구매",
            description = "유저는 광고를 1회 보고 포획 도구 한 개를 랜덤으로 지급, 및 정보 저장",
            responses = {
                    @ApiResponse(responseCode = "200", description = "랜덤 포획도구 정보")
            }
    )
    @PostMapping("/buy/random-capture-item")
    public ResponseEntity<RandomCaptureItemResponse> buyRandomCaptureItem() {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "코인 구매",
            description = "코인을 구매한다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    @PostMapping("/buy/coin")
    public ResponseEntity<Void> buyCoin(@RequestBody PurchaseRequest request) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "티켓 구매",
            description = "가챠 티켓을 구매한다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "400", description = "코인이 부족합니다.")
            }
    )
    @PostMapping("/buy/ticket")
    public ResponseEntity<Void> buyTicket(@RequestBody PurchaseRequest request) {
        return ResponseEntity.ok().build();
    }

}
