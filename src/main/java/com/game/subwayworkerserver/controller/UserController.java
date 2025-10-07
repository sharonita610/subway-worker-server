package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.controller.request.GpsRequest;
import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 정보 및 프로필 관리 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    @Operation(
            summary = "최초 사용자 등록",
            description = "유저이름과 유저가 선택한 동료 속성을 받아 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록 성공")
            }
    )
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "home 에서 사용되는 사용자 정보 조회",
            description = "익명 ID (UUID)를 기반으로 사용자 기본 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적인 정보 조회"),
                    @ApiResponse(responseCode = "400", description = "필수 헤더(X-User-ID) 누락 또는 유효하지 않은 ID",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserInfo(
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "요청받은 좌표기준 기준 랜덤 생성된 아이템 리스트 조회",
            description = "유저의 좌표 기준 5km 반경에 깔린 아이템의 리스트를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좌표와 아이템을 리스트 조회")
            }
    )
    @PostMapping("/gps-info")
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
}
