package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.GpsRequest;
import com.game.subwayworkerserver.controller.response.ItemLocationResponse;
import com.game.subwayworkerserver.controller.response.RandomGpsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.game.subwayworkerserver.controller.request.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsService {

    // RedisTemplate은 String, String 타입을 사용한다고 가정합니다.
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Key 상수 정의 (게임 아이템, 동료, 덫 등)
    private static final String FRIEND_LOCATIONS_KEY = "friend:locations";
    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final int MAX_ITEMS_TO_RETURN = 10;
    /**
     * 1. 요청받은 좌표 기준 반경 내에 있는 아이템 리스트를 Redis GEOSEARCH를 통해 조회합니다.
     * (사용자가 맵에 접근할 때마다 호출되는 핵심 조회 API)
     */
    public RandomGpsResponse findItemsInArea(GpsRequest request) {

        // 1. 검색 영역 설정
        Circle searchArea = new Circle(
                new Point(request.longitude(), request.latitude()),
                new Distance(request.rangeKm(), RedisGeoCommands.DistanceUnit.KILOMETERS)
        );

        // 2. GeoRadiusCommandArgs 옵션 설정
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance() // 거리를 포함
                .sort(Sort.Direction.ASC) // 거리 순으로 정렬
                .limit(MAX_ITEMS_TO_RETURN); // <--- 지도에 표시할 최대 개수 제한!

        // 3. 동료 위치 조회 (FRIEND_LOCATIONS_KEY 사용)
        List<ItemLocationResponse> friends = findLocationsByGeoKey(FRIEND_LOCATIONS_KEY, searchArea, args, "friend");

        // 4. 선물상자 위치 조회 (RANDOM_GIFT_BOX_KEY 사용)
        List<ItemLocationResponse> giftBoxes = findLocationsByGeoKey(RANDOM_GIFT_BOX_KEY, searchArea, args, "giftbox");

        return new RandomGpsResponse(friends, giftBoxes);
    }

    /**
     * Redis Key를 기반으로 GeoSpatial 검색을 수행하는 헬퍼 메서드.
     */
    private List<ItemLocationResponse> findLocationsByGeoKey(
            String geoKey,
            Circle searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs args,
            String entityType // 동료인지 선물상자인지 구분
    ) {
        // Redis GEOSEARCH 실행 (4인수 radius 시그니처 사용 가정)
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        geoKey,
                        searchArea,
                        args
                );

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        return results.getContent().stream()
                .map(result -> mapToItemLocationResponse(result, entityType)) // entityType을 매핑에 전달
                .toList();
    }

    /**
     * GeoResult 객체를 ItemLocationResponse DTO로 매핑하는 헬퍼 메서드
     */
    private ItemLocationResponse mapToItemLocationResponse(
            GeoResult<RedisGeoCommands.GeoLocation<String>> result,
            String entityType
    ) {
        String itemId = result.getContent().getName();
        Double distance = result.getDistance().getValue();

        // 이 부분에서 실제 좌표 (GEOPOS) 및 상세 정보를 조회해야 합니다.
        // 현재는 임시 값 사용
        Double itemLongitude = 127.0;
        Double itemLatitude = 37.5;
        String itemFeature = entityType;
        String itemName = entityType.equals("friend") ? "랜덤 동료" : "랜덤 선물상자";

        return new ItemLocationResponse(
                itemName,
                itemLatitude,
                itemLongitude,
                itemFeature,
                distance
        );
    }


    /**
     * 2. 좌표에 있는 랜덤 박스 조회 및 선물 지급 (/gps/random-gift)
     * (선물을 지급하면 Redis에서 박스 위치를 삭제해야 합니다.)
     */
//    public GpsRandomGiftResponse checkAndGrantRandomGift(GpsRandomGiftRequest request) {
//        // 실제로는 request.latitude()와 request.longitude()를 기반으로
//        // 해당 위치에 '랜덤 박스'가 존재하는지 Redis GEOSEARCH를 통해 매우 작은 반경(예: 5m)으로 검색해야 합니다.
//
//        // 여기서는 간단하게 해당 좌표 근처의 박스를 확인했다고 가정합니다.
//        String giftBoxId = "BOX-" + UUID.randomUUID().toString().substring(0, 8); // 임시 박스 ID
//
//        // 1. 해당 좌표에 랜덤 박스가 있는지 확인 및 가져오기 (실제 로직에서는 Redis GeoSearch 필요)
//        boolean isBoxPresent = Math.random() < 0.3;
//
//        if (isBoxPresent) {
//            // 2. 랜덤 선물 지급 로직
//            String giftName = "Gold Coin";
//            int amount = (int) (Math.random() * 100) + 50;
//
//            // 3. 지급된 박스는 해당 위치에서 삭제 (매우 중요: 중복 획득 방지)
//            redisTemplate.opsForGeo().remove(RANDOM_GIFT_BOX_KEY, giftBoxId);
//
//            return new GpsRandomGiftResponse(
//                    giftName,
//                    amount,
//                    "축하합니다! 랜덤 박스에서 " + amount + "개의 " + giftName + "을(를) 획득했습니다."
//            );
//        } else {
//            return new GpsRandomGiftResponse(null, 0, "현재 좌표에는 랜덤 박스가 없습니다.");
//        }
//    }
//
//    /**
//     * 3. 좌표에 덫을 설치 (/gps/trap)
//     * (덫 위치를 Redis에 저장하고, 이후 동료 생성 및 포획 로직에 사용됩니다.)
//     */
//    public void addTrap(GpsAddTrapRequest request) {
//        // 1. 사용자 재화/아이템 소모 확인 (DB 로직 필요)
//
//        // 2. Redis GeoAdd를 사용하여 덫의 위치를 저장
//        Point trapLocation = new Point(request.longitude(), request.latitude());
//
//        // 덫 Member ID 형식: trapType:userId:UUID
//        String trapMemberId = request.trapType() + ":" + UUID.randomUUID();
//        redisTemplate.opsForGeo().add(FRIEND_LOCATIONS_KEY, trapLocation, trapMemberId); // 덫을 동료 포획 키에 포함하거나 별도 키 사용
//
//        // 3. 덫 설치 성공 로깅
//        // System.out.println("Trap of type " + request.trapType() + " added.");
//    }
//
//    /**
//     * 4. 포획 가능한 동료 정보 조회 (/gps/capture-available-friend)
//     * (주변에 동료가 있는지 검색하며, 있다면 포획 확률 정보를 제공합니다.)
//     */
//    public CaptureAvailableFriendResponse getCaptureAvailableFriend(CaptureAvailableFriendRequest request) {
//        // 1. 사용자 위치 기반 근처 동료 검색 (Redis GeoSearch on FRIEND_LOCATIONS_KEY)
//        // GeoSearch를 통해 사용자 위치 근처 1km 이내의 동료를 검색한다고 가정
//        Circle searchArea = new Circle(
//                new Point(request.longitude(), request.latitude()),
//                new Distance(1.0, RedisGeoCommands.DistanceUnit.KILOMETERS)
//        );
//
//        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
//                .search(FRIEND_LOCATIONS_KEY, searchArea);
//
//        if (results != null && !results.getContent().isEmpty()) {
//            GeoResult<RedisGeoCommands.GeoLocation<String>> nearestFriend = results.getContent().get(0);
//            String friendId = nearestFriend.getContent().getName();
//
//            return new CaptureAvailableFriendResponse(
//                    friendId,
//                    "희귀 동료",
//                    nearestFriend.getDistance().getValue(),
//                    0.65 // 포획 확률 (아이템, 동료 레벨 등에 따라 달라져야 함)
//            );
//        } else {
//            return new CaptureAvailableFriendResponse(null, "주변에 포획 가능한 동료가 없습니다.", 0.0, 0.0);
//        }
//    }
//
//    /**
//     * 5. 동료 포획 요청 (/gps/capture)
//     * (포획 도구에 따른 확률 계산 및 성공 시 동료 삭제/지급 로직을 수행합니다.)
//     */
//    public CaptureResponse captureFriend(CaptureFriendRequest request) {
//        // 1. 동료 존재 여부 및 포획 도구 유효성 검사 (DB/Redis 로직)
//
//        // 2. 포획 확률 계산 로직 (도구 ID에 따른 확률 차등 적용)
//        double captureChance = switch (request.captureToolId()) {
//            case "BASIC_NET" -> 0.40;
//            case "GREAT_NET" -> 0.65;
//            case "MASTER_NET" -> 0.90;
//            default -> 0.0;
//        };
//
//        double randomRoll = Math.random();
//
//        if (randomRoll < captureChance) {
//            // 3. 포획 성공
//            // - 동료 위치 삭제 (Redis GEOREM - 가장 중요!)
//            redisTemplate.opsForGeo().remove(FRIEND_LOCATIONS_KEY, request.friendIdToCapture());
//
//            // - 사용자에게 동료 및 경험치 지급 (DB 트랜잭션 필요)
//            return new CaptureResponse(
//                    true,
//                    "동료 포획에 성공했습니다!",
//                    "새로운 역무원",
//                    200
//            );
//        } else {
//            // 4. 포획 실패
//            // - 포획 도구 내구도 감소 또는 파손 (DB 트랜잭션 필요)
//            return new CaptureResponse(
//                    false,
//                    "아쉽게도 동료가 도망쳤습니다. 도구를 업그레이드 해보세요.",
//                    null,
//                    10 // 실패 보상 경험치
//            );
//        }
//    }
}