package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.dto.response.market.MarketCoworkerListResponse;
import com.game.subwayworkerserver.dto.response.market.MarketSkillCardListResponse;
import com.game.subwayworkerserver.dto.response.market.PaymentSuccessResponse;
import com.game.subwayworkerserver.config.CurrentUuid;
import com.game.subwayworkerserver.dto.request.market.CoworkerFilterRequest;
import com.game.subwayworkerserver.dto.request.market.SellingRequest;
import com.game.subwayworkerserver.dto.request.market.SkillCardFilterRequest;
import com.game.subwayworkerserver.service.market.CoworkerMarketService;
import com.game.subwayworkerserver.service.market.SkillCardMarketService;
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

    @Operation(summary = "영입 가능한 동료 리스트 조회", description = "장터에 올라온 모든 동료의 리스트를 필터링 기능을 포함하여 조회")
    @PostMapping("/market/global/coworker")
    public ResponseEntity<MarketCoworkerListResponse> getCoworkersOnSale(@CurrentUuid String uuid, @Nullable @RequestBody CoworkerFilterRequest request) {
        return ResponseEntity.ok(coworkerMarketService.getCoworkerMarket(uuid, request));
    }

    @Operation(summary = "내가 판매 중인 동료 리스트 조회", description = "내가 판매 중인 동료의 리스트를 포함하여 조회")
    @PostMapping("/market/coworker/selling-list")
    public ResponseEntity<MarketCoworkerListResponse> getMyCoworkerSellingList(@CurrentUuid String uuid){
        return ResponseEntity.ok(coworkerMarketService.getMyCoworkerOnSale(uuid));
    }

    @Operation(summary = "장터에서 동료 영입하기", description = "장터에 올라온 동료를 영입하기")
    @PostMapping("/market/global/buy-coworker")
    public ResponseEntity<Void> buyCoworker(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        coworkerMarketService.purchaseCoworker(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "나의 동료 장터에 내놓기", description = "나의 동료를 장터에 내놓기 위한 기능")
    @PostMapping("/market/coworker/sell")
    public ResponseEntity<HttpStatus> sellCoworker(@CurrentUuid String uuid, @RequestBody SellingRequest request){
        coworkerMarketService.registerCoworkerOnMarket(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "장터에 내놓은 동료 철회", description = "장터에 내놓았던 동료를 철회하는 기능")
    @PostMapping("/market/coworker/cancel")
    public ResponseEntity<Void> cancelSellingCoworker(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        coworkerMarketService.cancelSellingMyCoworker(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "판매 완료된 동료 정산", description = "장터에 내놓았던 동료가 판매되어 정산 요청")
    @GetMapping("/market/coworker/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getCoworkerPaid(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        return ResponseEntity.ok(coworkerMarketService.getCoworkerSoldPayment(uuid, marketId));
    }

    @Operation(summary = "구매 가능한 스킬카드 리스트 조회", description = "장터에 올라온 모든 스킬 카드의 리스트를 필터링 기능을 포함하여 조회")
    @PostMapping("/market/global/skill")
    public ResponseEntity<MarketSkillCardListResponse> getGlobalSkillCardOnSale(@CurrentUuid String uuid,
            @Nullable @RequestBody SkillCardFilterRequest request
    ){
        return ResponseEntity.ok(skillCardMarketService.getSkillCardMarket(uuid, request));
    }

    @Operation(summary = "내가 판매 중인 스킬카드 리스트 조회", description = "내가 판매 중인 스킬카드 리스트를 조회")
    @PostMapping("/market/skill/selling-list")
    public ResponseEntity<MarketSkillCardListResponse> getMySkillSellingList(@CurrentUuid String uuid){
        return ResponseEntity.ok(skillCardMarketService.getMySkillCardOnSale(uuid));
    }

    @Operation(summary = "장터에서 스킬카드 구매하기", description = "장터에 올라온 스킬카드를 구매하기")
    @PostMapping("/market/global/buy-skill")
    public ResponseEntity<Void> buySkillCard(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        skillCardMarketService.purchaseSkillCard(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "나의 스킬카드 장터에 내놓기", description = "나의 스킬카드 장터에 내놓기 위한 기능")
    @PostMapping("/market/skill/sell")
    public ResponseEntity<HttpStatus> sellSkillCard(@CurrentUuid String uuid, @RequestBody SellingRequest request){
        skillCardMarketService.registerCardOnMarket(uuid, request);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "장터에 내놓은 스킬카드 판매 철회", description = "장터에 내놓았던 스킬카드를 철회하는 기능")
    @PostMapping("/market/skill/cancel")
    public ResponseEntity<HttpStatus> cancelSellingSkill(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        skillCardMarketService.cancelSellingMySkillCard(uuid, marketId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "판매 완료된 스킬카드 정산", description = "장터에 내놓았던 스킬카드가 판매되어 정산 요청")
    @GetMapping("/market/skill/get-paid")
    public ResponseEntity<PaymentSuccessResponse> getSillPaid(@CurrentUuid String uuid, @RequestParam("id") long marketId){
        return ResponseEntity.ok(skillCardMarketService.getSkillCardSoldPayment(uuid, marketId));
    }
}
