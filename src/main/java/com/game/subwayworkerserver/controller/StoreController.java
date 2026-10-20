package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.dto.request.store.PurchaseRequest;
import com.game.subwayworkerserver.dto.response.item.ItemResponse;
import com.game.subwayworkerserver.dto.response.item.StoreItemResponse;
import com.game.subwayworkerserver.service.store.StoreService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "store 조회", description = "store에서 구매 가능한 모든 아이템의 리스트를 조회")
    @GetMapping("/store")
    public ResponseEntity<StoreItemResponse> getStoreItems(@CurrentUuid String uuid) {
        return ResponseEntity.ok(storeService.getStoreItems(uuid));
    }

    @Operation(summary = "아이템 구매", description = "구매하고자 포획 또는 덫의 정보를 받아 코인으로 구매")
    @PostMapping("/store/item")
    public ResponseEntity<Void> buyTrapItem(@CurrentUuid String uuid, @RequestBody PurchaseRequest request) {
        storeService.purchaseItemWithCoin(uuid, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "광고를 보고 아이템 구매 하기 (외부 광고 API 제외)", description = "광고를 보고 랜덤 아이템을 얻는 기능")
    @PostMapping("/store/random-item")
    public ResponseEntity<ItemResponse> buyRandomItem(@CurrentUuid String uuid, @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(storeService.purchaseRandomItem(uuid, request));
    }

    @Operation(summary = "코인 구매", description = "휴가 티켓으로 코인을 구매한다")
    @PostMapping("/store/coin")
    public ResponseEntity<Void> buyCoin(@CurrentUuid String uuid, @RequestBody PurchaseRequest request) {
        storeService.purchaseCoinWithVacationTicket(uuid, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "휴가 티켓 구매 (실제 결제 제외)", description = "현금으로 휴가 티켓을 구매한다")
    @PostMapping("/store/vacation-ticket")
    public ResponseEntity<Void> buyTicket(@CurrentUuid String uuid, @RequestBody PurchaseRequest request) {
        storeService.buyVacationTicket(uuid, request);
        return ResponseEntity.ok().build();
    }

}
