package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.FriendFilterRequest;
import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.response.*;
import com.game.subwayworkerserver.service.MarketService;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Market API", description = "장터 API")
@RestController
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @Operation(
            summary = "영입 가능한 동료 리스트 조회 [완]",
            description = "장터에 올라온 모든 동료의 리스트를 필터링 기능을 포함하여 조회"
    )
    @PostMapping("/market/global/friends")
    public ResponseEntity<GlobalFriendsResponse> getFriendsOnSale(@Nullable @RequestBody FriendFilterRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(marketService.getFriendMarket(uuid, request));
    }

    @Operation(
            summary = "내가 판매 중인 동료 리스트 조회 [완]",
            description = "내가 판매 중인 동료의 리스트를 포함하여 조회"
    )
    @PostMapping("/market/friend/selling-list")
    public ResponseEntity<MyFriendsMarketResponse> getMyFriendSellingList(){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(marketService.getMyFriendsOnSale(uuid));
    }

    @Operation(
            summary = "장터에서 동료 영입하기 [완]",
            description = "장터에 올라온 동료를 영입하기"
    )
    @PostMapping("/market/global/buy-friend")
    public ResponseEntity<Void> buyFriend(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        marketService.purchaseFriend(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "나의 동료 장터에 내놓기 [완]",
            description = "나의 동료를 장터에 내놓기 위한 기능"
    )
    @PostMapping("/market/friend/sell")
    public ResponseEntity<HttpStatus> sellFriend(@RequestBody SellingRequest request){
        String uuid = SecurityUtils.getCurrentUserUuid();
        marketService.registerFriendOnMarket(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "장터에 내놓은 동료 철회 [완]",
            description = "장터에 내놓았던 동료를 철회하는 기능"
    )
    @PostMapping("/market/friend/cancel")
    public ResponseEntity<Void> cancelSellingFriend(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        marketService.cancelSellingMyFriend(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "판매 완료된 동료 정산 [완]",
            description = "장터에 내놓았던 동료가 판매되어 정산 요청"
    )
    @GetMapping("/market/friend/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getFriendPaid(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(marketService.getFriendSoldPayment(uuid, marketId));
    }

    @Operation(
            summary = "구매 가능한 스킬카드 리스트 조회",
            description = "장터에 올라온 모든 스킬 카드의 리스트를 필터링 기능을 포함하여 조회"
    )
    @PostMapping("/market/global/skill")
    public ResponseEntity<GlobalSkillCardResponse> getGlobalSkillCardOnSale(){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내가 판매 중인 스킬카드 리스트 조회",
            description = "내가 판매 중인 스킬카드 리스트를 조회"
    )
    @PostMapping("/market/skill/selling-list")
    public ResponseEntity<SkillOnSaleResponse> getMySkillSellingList(){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "장터에서 스킬카드 구매하기",
            description = "장터에 올라온 스킬카드를 구매하기"
    )
    @PostMapping("/market/global/buy-skill")
    public ResponseEntity<Void> buySkillCard(@RequestParam("id") long skillId){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "나의 스킬카드 장터에 내놓기",
            description = "나의 스킬카드 장터에 내놓기 위한 기능"
    )
    @PostMapping("/market/skill/sell")
    public ResponseEntity<Void> sellSkill(@RequestBody SellingRequest request){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "장터에 내놓은 스킬카드 판매 철회",
            description = "장터에 내놓았던 스킬카드를 철회하는 기능"
    )
    @PostMapping("/market/skill/cancel")
    public ResponseEntity<Void> cancelSellingSkill(@RequestParam("id") long skillId){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "판매 완료된 스킬카드 정산",
            description = "장터에 내놓았던 스킬카드가 판매되어 정산 요청"
    )
    @GetMapping("/market/skill/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getSillPaid(@RequestParam("id") long skillId){
        String uuid = SecurityUtils.getCurrentUserUuid();

        return ResponseEntity.ok().build();
    }
}
