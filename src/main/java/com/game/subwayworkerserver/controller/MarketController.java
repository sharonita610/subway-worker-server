package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.config.SecurityUtils;
import com.game.subwayworkerserver.controller.request.CoworkerFilterRequest;
import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.controller.response.market.*;
import com.game.subwayworkerserver.service.CoworkerMarketService;
import com.game.subwayworkerserver.service.SkillCardMarketService;
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

    private final CoworkerMarketService coworkerMarketService;
    private final SkillCardMarketService skillCardMarketService;

    @Operation(
            summary = "영입 가능한 동료 리스트 조회 [완]",
            description = "장터에 올라온 모든 동료의 리스트를 필터링 기능을 포함하여 조회"
    )
    @PostMapping("/market/global/coworker")
    public ResponseEntity<GlobalCoworkerResponse> getCoworkersOnSale(@Nullable @RequestBody CoworkerFilterRequest request) {
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(coworkerMarketService.getCoworkerMarket(uuid, request));
    }

    @Operation(
            summary = "내가 판매 중인 동료 리스트 조회 [완]",
            description = "내가 판매 중인 동료의 리스트를 포함하여 조회"
    )
    @PostMapping("/market/coworker/selling-list")
    public ResponseEntity<MyCoworkersMarketResponse> getMyCoworkerSellingList(){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(coworkerMarketService.getMyCoworkerOnSale(uuid));
    }

    @Operation(
            summary = "장터에서 동료 영입하기 [완]",
            description = "장터에 올라온 동료를 영입하기"
    )
    @PostMapping("/market/global/buy-coworker")
    public ResponseEntity<Void> buyCoworker(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        coworkerMarketService.purchaseCoworker(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "나의 동료 장터에 내놓기 [완]",
            description = "나의 동료를 장터에 내놓기 위한 기능"
    )
    @PostMapping("/market/coworker/sell")
    public ResponseEntity<HttpStatus> sellCoworker(@RequestBody SellingRequest request){
        String uuid = SecurityUtils.getCurrentUserUuid();
        coworkerMarketService.registerCoworkerOnMarket(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "장터에 내놓은 동료 철회 [완]",
            description = "장터에 내놓았던 동료를 철회하는 기능"
    )
    @PostMapping("/market/coworker/cancel")
    public ResponseEntity<Void> cancelSellingCoworker(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        coworkerMarketService.cancelSellingMyCoworker(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "판매 완료된 동료 정산 [완]",
            description = "장터에 내놓았던 동료가 판매되어 정산 요청"
    )
    @GetMapping("/market/coworker/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getCoworkerPaid(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(coworkerMarketService.getCoworkerSoldPayment(uuid, marketId));
    }

    @Operation(
            summary = "구매 가능한 스킬카드 리스트 조회 [완]",
            description = "장터에 올라온 모든 스킬 카드의 리스트를 필터링 기능을 포함하여 조회"
    )
    @PostMapping("/market/global/skill")
    public ResponseEntity<GlobalSkillCardResponse> getGlobalSkillCardOnSale(
            @Nullable @RequestBody SkillCardFilterRequest request
    ){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardMarketService.getSkillCardMarket(uuid, request));
    }

    @Operation(
            summary = "내가 판매 중인 스킬카드 리스트 조회 [완]",
            description = "내가 판매 중인 스킬카드 리스트를 조회"
    )
    @PostMapping("/market/skill/selling-list")
    public ResponseEntity<SkillOnSaleResponse> getMySkillSellingList(){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardMarketService.getMySkillCardOnSale(uuid));
    }

    @Operation(
            summary = "장터에서 스킬카드 구매하기 [완]",
            description = "장터에 올라온 스킬카드를 구매하기"
    )
    @PostMapping("/market/global/buy-skill")
    public ResponseEntity<Void> buySkillCard(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        skillCardMarketService.purchaseSkillCard(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "나의 스킬카드 장터에 내놓기 [완]",
            description = "나의 스킬카드 장터에 내놓기 위한 기능"
    )
    @PostMapping("/market/skill/sell")
    public ResponseEntity<HttpStatus> sellSkillCard(@RequestBody SellingRequest request){
        String uuid = SecurityUtils.getCurrentUserUuid();
        skillCardMarketService.registerCardOnMarket(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "장터에 내놓은 스킬카드 판매 철회 [완]",
            description = "장터에 내놓았던 스킬카드를 철회하는 기능"
    )
    @PostMapping("/market/skill/cancel")
    public ResponseEntity<HttpStatus> cancelSellingSkill(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        skillCardMarketService.cancelSellingMySkillCard(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "판매 완료된 스킬카드 정산 [완]",
            description = "장터에 내놓았던 스킬카드가 판매되어 정산 요청"
    )
    @GetMapping("/market/skill/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getSillPaid(@RequestParam("id") long marketId){
        String uuid = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(skillCardMarketService.getSkillCardSoldPayment(uuid, marketId));
    }
}
