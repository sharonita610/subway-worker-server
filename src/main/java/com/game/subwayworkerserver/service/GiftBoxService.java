package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.GpsRandomGiftRequest;
import com.game.subwayworkerserver.controller.response.GpsRandomGiftResponse;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import com.game.subwayworkerserver.entity.ItemName;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserItemMapEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 어노테이션 사용 가정

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserRepository userRepository;
    private final UserItemMapRepository userItemMapRepository;

    private static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    private static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    private static final double SEARCH_RADIUS_METER = 50.0;

    // --- 보상 상수 (이전 답변에서 사용된 것 재활용) ---
    private static final int BASE_EXP = 20;
    private static final long COIN_MIN = 10000;
    private static final long COIN_MAX = 200000;
    private static final int TICKET_MIN = 3;
    private static final int TICKET_MAX = 200;

    @Transactional
    public GpsRandomGiftResponse getRandomGift(GpsRandomGiftRequest request, String userUuid) {

        // 1. Redis에서 위도/경도를 가지고 아이템(선물 상자) 찾기
        String giftBoxId = findNearestGiftBoxId(request.latitude(), request.longitude());

        if (giftBoxId == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "50m 반경 내에 선물 상자가 없습니다.");
        }

        // 2. 찾은 아이템을 유저별 획득 기록에 저장하기 (중복 획득 방지)
        String acquiredKey = USER_ACQUIRED_PREFIX + userUuid + ":" + RANDOM_GIFT_BOX_KEY;

        // 이미 획득했는지 확인
        if (redisTemplate.opsForSet().isMember(acquiredKey, giftBoxId)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 획득한 선물 상자입니다.");
        }

        // 획득 기록 저장 (A user가 획득하면 A user의 Set에만 저장)
        redisTemplate.opsForSet().add(acquiredKey, giftBoxId);

        // 3. 기본 선물 랜덤 뽑기 및 4. 추가 선물 랜덤 뽑기
        List<DefaultGift> defaultGifts = calculateDefaultRewards();
        List<ExtraGift> bonusGifts = calculateBonusRewards();

        // 5. DB 업데이트 로직 (유저 재화 증가) 필요
        updateUserRewardsAndItems(userUuid, defaultGifts, bonusGifts);
        return GpsRandomGiftResponse.of(defaultGifts, bonusGifts);
    }

    private void updateUserRewardsAndItems(String userUuid, List<DefaultGift> defaultGifts, List<ExtraGift> bonusGifts) {
        UserEntity user = userRepository.findByUuid(userUuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );

        // 2. 기본 재화 합산
        int totalCoins = 0;
        int totalTickets = 0;
        int totalExperience = 0;

        for (DefaultGift gift : defaultGifts) {
            switch (gift.name()) {
                case "coin":
                    totalCoins += gift.quantity();
                    break;
                case "vacationTicket":
                    totalTickets += gift.quantity();
                    break;
                case "exp":
                    totalExperience += gift.quantity();
                    break;
            }
        }

        // 3. 유저 잔고 업데이트
        user.updateRewards(totalCoins, totalTickets, totalExperience);
        // userRepository.save(user); // @Transactional 덕분에 자동 반영됨

        // 4. 추가 아이템 인벤토리 업데이트 (재료, 도구 등)
        for (ExtraGift item : bonusGifts) {

            if(item.name().equals("gotchaTicket")) {
                user.increaseGotchaTicket();
                continue;
            }
            ItemName itemName = ItemName.fromKoreanName(item.name());
            userItemMapRepository.findByUserIdAndItemName(user.getId(), itemName)
                    .ifPresentOrElse(
                            UserItemMapEntity::increaseQuantity,

                            // 가지고 있지 않은 경우: 새로운 항목 생성
                            () -> {
                                UserItemMapEntity newMap = UserItemMapEntity.of(user.getId(), itemName);
                                userItemMapRepository.save(newMap);
                            }
                    );
        }
    }


    /**
     * Helper: 사용자 좌표에서 가장 가까운 선물 상자 ID를 검색합니다. (요청하신 findNearestGiftBoxId 그대로 사용)
     */
    private String findNearestGiftBoxId(double lat, double lon) {
        Circle searchArea = new Circle(
                new Point(lon, lat),
                new Distance(SEARCH_RADIUS_METER, RedisGeoCommands.DistanceUnit.METERS)
        );

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().limit(1);

        // 3인수 radius 시그니처가 없으므로, 3인수 radius(Key, Circle, Args)를 사용합니다.
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(
                        RANDOM_GIFT_BOX_KEY,
                        searchArea,
                        args
                );

        if (results == null || results.getContent().isEmpty()) {
            return null;
        }
        return results.getContent().get(0).getContent().getName();
    }

    private List<DefaultGift> calculateDefaultRewards() {
        // 코인 (기대값 30,000, 범위 1만~20만)
        // 낮은 값에 치우친 분포를 만들기 위해 높은 skewFactor(3.5)를 적용합니다.
        final double SKEW_FACTOR = 3.5;
        final long COIN_RANGE = COIN_MAX - COIN_MIN;

        // 1. 0~1 사이의 난수를 생성하고 SKEW_FACTOR 제곱하여 0에 가깝게 만듭니다.
        double skewedRandomCoin = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), SKEW_FACTOR);
        // 2. 이 값을 범위에 적용하여 낮은 코인 값을 생성합니다.
        long coins = (long) (COIN_MIN + skewedRandomCoin * COIN_RANGE);


        // 휴가티켓 (기대값 20, 범위 3~200)
        final int TICKET_RANGE = TICKET_MAX - TICKET_MIN;
        double skewedRandomTicket = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN + skewedRandomTicket * TICKET_RANGE);

        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));
        defaultGiftList.add(new DefaultGift("exp", "경험치", BASE_EXP));

        return defaultGiftList;
    }

    private List<ExtraGift> calculateBonusRewards() {
        List<ExtraGift> rewards = new ArrayList<>();

        // 아이템 종류별 확률 정의 (Integer는 확률을 % 단위로 사용)
        Map<String, Integer> probabilityMap = Map.of(
                // ItemName.PAPER_BOX와 ItemName.BRIEFCASE는 포획 도구, SHOVEL은 트랩 도구입니다.
                // 확률표의 종류에 맞게 기존 ItemName을 매핑하여 사용합니다.

                // 하급 채집툴 1개: 60% (WOOD_SHOVEL로 대체)
                ItemName.WOOD_SHOVEL.getName(), 60,

                // 중급 채집툴 1개: 10% (SILVER_SHOVEL로 대체)
                ItemName.SILVER_SHOVEL.getName(), 10,

                // 하급 덫 1개: 30% (PAPER_BOX로 대체)
                ItemName.PAPER_BOX.getName(), 30,

                // 중급 덫 1개: 5% (BRIEFCASE로 대체)
                ItemName.BRIEFCASE.getName(), 5,

                // 럭키 가챠 티켓 1개: 10% (BUSINESS_CARRIER로 대체)
                "gotchaTicket", 10
        );

        // Map을 순회하며 각 아이템에 대해 독립적으로 확률을 검사합니다.
        for (Map.Entry<String, Integer> entry : probabilityMap.entrySet()) {
            String item = entry.getKey();
            int requiredProbability = entry.getValue();

            // 0.0 ~ 100.0 사이의 난수 생성
            double roll = ThreadLocalRandom.current().nextDouble(0, 100);

            // 난수(roll)가 요구 확률보다 작거나 같으면 획득 성공 (예: 60% 확률이면 roll이 60.0 미만)
            if (roll < requiredProbability) {
                // 획득 성공 시 ExtraGift DTO 생성
                rewards.add(new ExtraGift(item, 1));
            }
        }
        return rewards;
    }
}