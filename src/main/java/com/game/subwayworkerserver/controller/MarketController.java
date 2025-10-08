package com.game.subwayworkerserver.controller;


import com.game.subwayworkerserver.controller.request.SellFriendRequest;
import com.game.subwayworkerserver.controller.response.FriendsOnSaleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Market API", description = "장터 API")
@RestController
@RequiredArgsConstructor
public class MarketController {

    @PostMapping("/market/global/friends-on-sale")
    public ResponseEntity<FriendsOnSaleResponse> getFriendsOnSale(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/market/my-selling-list")
    public ResponseEntity<FriendsOnSaleResponse> getMySellingList(

    ){
        return ResponseEntity.ok().build();
    }


    @PostMapping("/market//friend/buy")
    public ResponseEntity<?>  getFriend(@RequestParam("id") long friendId){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/market/friend/sell")
    public ResponseEntity<?> sellFriend(
            @RequestParam("id") long friendId,
            @RequestBody SellFriendRequest request

    ){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/market/friend/cancel")
    public ResponseEntity<?> cancelSellingFriend(@RequestParam("id") long friendId){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/market/friend/get-paid")
    public ResponseEntity<?> getPaid(@RequestParam("id") long friendId){
        return ResponseEntity.ok().build();
    }

}
