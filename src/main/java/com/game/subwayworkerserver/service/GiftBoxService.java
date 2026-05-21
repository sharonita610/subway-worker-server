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
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserFinder userFinder;
    private final UserItemMapRepository userItemMapRepository;
    private final UserLevelProcessor userLevelProcessor;

    private static final String RANDOM_GIFT_BOX_KEY = GpsRedisKeys.RANDOM_GIFT_BOX_KEY;
    private static final String USER_ACQUIRED_PREFIX = GpsRedisKeys.USER_ACQUIRED_PREFIX;
    private static final double SEARCH_RADIUS_METER = 50.0;

    private static final int BASE_EXP = 20;
    private static final long ACQUIRED_KEY_TTL_DAYS = 5;

     private static final long COIN_MIN_ADJUSTED = 10000;
     private static final long COIN_MAX_ADJUSTED = 80000;
     private static final int TICKET_MIN_ADJUSTED = 3;
     private static final int TICKET_MAX_ADJUSTED = 200;

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

        redisTemplate.expire(acquiredKey, ACQUIRED_KEY_TTL_DAYS, TimeUnit.DAYS);

        // 3. 기본 선물 랜덤 뽑기 및 4. 추가 선물 랜덤 뽑기
        List<DefaultGift> defaultGifts = calculateDefaultRewards();
        List<ExtraGift> bonusGifts = calculateBonusRewards();

        // 5. DB 업데이트 로직 (유저 재화 증가) 필요
        updateUserRewardsAndItems(userUuid, defaultGifts, bonusGifts);
        return GpsRandomGiftResponse.of(defaultGifts, bonusGifts);
    }

    private void updateUserRewardsAndItems(String userUuid, List<DefaultGift> defaultGifts, List<ExtraGift> bonusGifts) {
        UserEntity user = userFinder.getByUuid(userUuid);

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
        user.increaseCoin(totalCoins);
        user.increaseVacationTicket(totalTickets);
        userLevelProcessor.processExpGainAndLevelUp(user, totalExperience);
        // 4. 추가 아이템 인벤토리 업데이트 (재료, 도구 등)
        for (ExtraGift item : bonusGifts) {

            if(item.name().equals("gachaTicket")) {
                user.increaseGachaTicket();
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

    private String findNearestGiftBoxId(double lat, double lon) {
        Circle searchArea = new Circle(
                new Point(lon, lat),
                new Distance(SEARCH_RADIUS_METER, RedisGeoCommands.DistanceUnit.METERS)
        );

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().limit(1);

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
        final double SKEW_FACTOR = 3.5;
        final double COIN_SKEW_FACTOR = 6.0;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        final long COIN_RANGE = COIN_MAX_ADJUSTED - COIN_MIN_ADJUSTED;

        double skewedRandomCoin = Math.pow(ThreadLocalRandom.current().nextDouble(0, 1), COIN_SKEW_FACTOR);
        long coins = (long) (COIN_MIN_ADJUSTED + skewedRandomCoin * COIN_RANGE);

        final int TICKET_RANGE = TICKET_MAX_ADJUSTED - TICKET_MIN_ADJUSTED;

        double skewedRandomTicket = Math.pow(random.nextDouble(0, 1), SKEW_FACTOR);
        int tickets = (int) (TICKET_MIN_ADJUSTED + skewedRandomTicket * TICKET_RANGE);

        List<DefaultGift> defaultGiftList = new ArrayList<>();
        defaultGiftList.add(new DefaultGift("coin", "코인", (int) coins));
        defaultGiftList.add(new DefaultGift("vacationTicket", "휴가티켓", tickets));
        defaultGiftList.add(new DefaultGift("exp", "경험치", BASE_EXP));

        return defaultGiftList;
    }

    private List<ExtraGift> calculateBonusRewards() {
        List<ExtraGift> rewards = new ArrayList<>();

        Map<String, Integer> probabilityMap = Map.of(

                ItemName.WOOD_SHOVEL.getName(), 50,
                ItemName.SILVER_SHOVEL.getName(), 10,
                ItemName.PAPER_BOX.getName(), 30,
                ItemName.BRIEFCASE.getName(), 5,
                "gachaTicket", 30
        );

        for (Map.Entry<String, Integer> entry : probabilityMap.entrySet()) {
            String item = entry.getKey();
            int requiredProbability = entry.getValue();

            double roll = ThreadLocalRandom.current().nextDouble(0, 100);

            if (roll < requiredProbability) {
                rewards.add(new ExtraGift(item, 1));
            }
        }
        return rewards;
    }
}