package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.common.UserLevelProcessor;
import com.game.subwayworkerserver.dto.request.gps.GpsRandomGiftRequest;
import com.game.subwayworkerserver.dto.response.gps.GpsRandomGiftResponse;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import com.game.subwayworkerserver.domain.reward.RewardRoller;
import com.game.subwayworkerserver.domain.reward.RewardRoller.Chance;
import com.game.subwayworkerserver.domain.item.ItemName;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserFinder userFinder;
    private final UserItemMapRepository userItemMapRepository;
    private final UserLevelProcessor userLevelProcessor;

    private static final double SEARCH_RADIUS_METER = 50.0;

    private static final int BASE_EXP = 20;
    private static final long ACQUIRED_KEY_TTL_DAYS = 5;

    private static final long COIN_MIN_ADJUSTED = 10000;
    private static final long COIN_MAX_ADJUSTED = 80000;
    private static final int TICKET_MIN_ADJUSTED = 3;
    private static final int TICKET_MAX_ADJUSTED = 200;
    private static final String GACHA_TICKET = "GACHA_TICKET";

    /** 상자를 열 때 후보마다 독립적으로 굴리는 보너스 보상. */
    private static final List<Chance> BONUS_REWARD_CHANCES = List.of(
            new Chance(ItemName.WOOD_SHOVEL.name(), 50),
            new Chance(ItemName.SILVER_SHOVEL.name(), 10),
            new Chance(ItemName.PAPER_BOX.name(), 30),
            new Chance(ItemName.BRIEFCASE.name(), 5),
            new Chance(GACHA_TICKET, 30)
    );

    @Transactional
    public GpsRandomGiftResponse getRandomGift(GpsRandomGiftRequest request, String userUuid) {

        String giftBoxId = findNearestGiftBoxId(request.latitude(), request.longitude());

        if (giftBoxId == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "50m 반경 내에 선물 상자가 없습니다.");
        }

        String acquiredKey = GpsRedisKeys.USER_ACQUIRED_PREFIX + userUuid + ":" + GpsRedisKeys.RANDOM_GIFT_BOX_KEY;

        if (redisTemplate.opsForSet().isMember(acquiredKey, giftBoxId)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 획득한 선물 상자입니다.");
        }

        // 획득 기록 저장 (A user가 획득하면 A user의 Set에만 저장)
        redisTemplate.opsForSet().add(acquiredKey, giftBoxId);

        redisTemplate.expire(acquiredKey, ACQUIRED_KEY_TTL_DAYS, TimeUnit.DAYS);

        List<DefaultGift> defaultGifts = calculateDefaultRewards();
        List<ExtraGift> bonusGifts = calculateBonusRewards();

        updateUserRewardsAndItems(userUuid, defaultGifts, bonusGifts);
        return GpsRandomGiftResponse.of(defaultGifts, bonusGifts);
    }

    private void updateUserRewardsAndItems(String userUuid, List<DefaultGift> defaultGifts, List<ExtraGift> bonusGifts) {
        UserEntity user = userFinder.getByUuid(userUuid);

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

        user.increaseCoin(totalCoins);
        user.increaseVacationTicket(totalTickets);
        userLevelProcessor.processExpGainAndLevelUp(user, totalExperience);
        for (ExtraGift item : bonusGifts) {

            if (item.name().equals(GACHA_TICKET)) {
                user.increaseGachaTicket();
                continue;
            }
            ItemName itemName = ItemName.fromConstantName(item.name());
            userItemMapRepository.findByUserIdAndItemName(user.getId(), itemName)
                    .ifPresentOrElse(
                            UserItemMapEntity::increaseQuantity,

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
                        GpsRedisKeys.RANDOM_GIFT_BOX_KEY,
                        searchArea,
                        args
                );

        if (results == null || results.getContent().isEmpty()) {
            return null;
        }
        return results.getContent().get(0).getContent().getName();
    }

    private List<DefaultGift> calculateDefaultRewards() {
        int coins = (int) RewardRoller.skewedBetween(COIN_MIN_ADJUSTED, COIN_MAX_ADJUSTED, RewardRoller.COIN_SKEW_FACTOR);
        int tickets = (int) RewardRoller.skewedBetween(TICKET_MIN_ADJUSTED, TICKET_MAX_ADJUSTED, RewardRoller.TICKET_SKEW_FACTOR);
        return List.of(
                new DefaultGift("coin", "코인", coins),
                new DefaultGift("vacationTicket", "휴가티켓", tickets),
                new DefaultGift("exp", "경험치", BASE_EXP)
        );
    }

    private List<ExtraGift> calculateBonusRewards() {
        return RewardRoller.rollEach(BONUS_REWARD_CHANCES).stream()
                .map(name -> new ExtraGift(name, 1))
                .toList();
    }
}
