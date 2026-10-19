package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.store.PurchaseRequest;
import com.game.subwayworkerserver.controller.response.item.ItemResponse;
import com.game.subwayworkerserver.controller.response.item.StoreItemResponse;
import com.game.subwayworkerserver.service.store.StoreService;
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

    private final StoreService storeService;

    @Operation(
            summary = "store 조회 [완]",
            description = "store에서 구매 가능한 모든 아이템의 리스트를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적인 정보 조회")
            }
    )
    @GetMapping("/store")
    public ResponseEntity<StoreItemResponse> getStoreItems() {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(storeService.getStoreItems(uuid));
    }

    @Operation(
            summary = "아이템 구매 [완]",
            description = "구매하고자 포획 또는 덫의 정보를 받아 코인으로 구매",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    @PostMapping("/store/item")
    public ResponseEntity<Void> buyTrapItem(@RequestBody PurchaseRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        storeService.purchaseItemWithCoin(uuid, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "광고를 보고 아이템 구매 하기 [외부 광고 api 제외 완]",
            description = "광고를 보고 랜덤 아이템을 얻는 기능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    @PostMapping("/store/random-item")
    public ResponseEntity<ItemResponse> buyRandomItem(@RequestBody PurchaseRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(storeService.purchaseRandomItem(uuid, request));
    }

    @Operation(
            summary = "코인 구매 [완]",
            description = "휴가 티켓으로 코인을 구매한다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    @PostMapping("/store/coin")
    public ResponseEntity<Void> buyCoin(@RequestBody PurchaseRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        storeService.purchaseCoinWithVacationTicket(uuid, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "휴가 티켓 구매 [실제 결제 제외 완]",
            description = "현금으로 휴가 티켓을 구매한다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
            }
    )
    @PostMapping("/store/vacation-ticket")
    public ResponseEntity<Void> buyTicket(@RequestBody PurchaseRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        storeService.buyVacationTicket(uuid, request);
        return ResponseEntity.ok().build();
    }

}
