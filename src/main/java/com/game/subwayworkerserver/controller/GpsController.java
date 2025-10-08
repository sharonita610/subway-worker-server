package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.request.*;
import com.game.subwayworkerserver.controller.response.CaptureAvailableFriendResponse;
import com.game.subwayworkerserver.controller.response.CaptureResponse;
import com.game.subwayworkerserver.controller.response.GpsRandomGiftResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GPS API", description = "GPS관련 API")
@RestController
@RequiredArgsConstructor
public class GpsController {

    @Operation(
            summary = "요청받은 좌표기준 기준 랜덤 생성된 아이템 리스트 조회",
            description = "유저의 좌표 기준 5km 반경에 깔린 아이템의 리스트를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좌표와 아이템을 리스트 조회")
            }
    )
    @PostMapping("/gps/mobs")
    public ResponseEntity<?> getGpsInfo(@RequestBody GpsRequest request){
        /**
         * // 1. 사용자 위치(Point) 및 검색 반경(Circle) 설정
         *         Point userLocation = new Point(request.longitude(), request.latitude());
         *         Distance distance = new Distance(request.rangeKm(), RedisGeoCommands.DistanceUnit.KILOMETERS);
         *         Circle searchArea = new Circle(userLocation, distance);
         *
         *         // 2. GEOSEARCH 옵션 설정: 거리와 좌표를 결과에 포함하도록 설정
         *         RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
         *                 .includeDistance() // 검색 결과에 거리를 포함
         *                 .sort(RedisGeoCommands.GeoSearchSort.BY_DISTANCE); // 거리 순으로 정렬
         *
         *         // 3. Redis GEOSEARCH 실행
         *         // key: item:locations, area: user's 5km circle, args: options
         *         GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
         *                 .search(ITEM_KEY, searchArea, args);
         *
         *         // 4. 결과 매핑 및 반환
         *         List<ItemLocation> itemList = results.getContent().stream()
         *                 .map(result -> new ItemLocation(
         *                         result.getContent().getName(),
         *                         result.getDistance().getValue() // 거리 값 (Km 단위)
         *                 ))
         *                 .toList();
         *
         *         return ResponseEntity.ok(itemList);
         */
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "좌표에 있는 랜덤 박스 조회",
            description = "gps의 좌표에 있는 랜덤 박스를 조회하여 랜덤 선물을 받는다."
    )
    @PostMapping("/gps/random-gift")
    public ResponseEntity<GpsRandomGiftResponse> getRandomGift(@RequestBody GpsRandomGiftRequest request){
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "좌표에 덫을 깔아놓는다.",
            description = "랜덤으로 생성되는 동료를 잡기위한 덫을 설정한다."
    )
    @PostMapping("/gps/trap")
    public ResponseEntity<Void> addTrap(@RequestBody GpsAddTrapRequest request){
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "좌표로 포획 가능한 동료의 정보를 조회",
            description = "gps의 좌표를 통해 현재 포획 가능한 동료의 정보를 조회하는 기능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포획 가능한 동료의 기본 정보를 조회")
            }
    )
    @PostMapping("/gps/capture-available-friend")
    public ResponseEntity<CaptureAvailableFriendResponse> getCaptureAvailableFriend(@RequestBody CaptureAvailableFriendRequest request) {
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
    @PostMapping("/gps/capture")
    public ResponseEntity<CaptureResponse> captureFriend(@RequestBody CaptureFriendRequest request) {
        return ResponseEntity.ok().build();
    }

}
