package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.dto.request.gps.GpsRandomGiftRequest;
import com.game.subwayworkerserver.dto.response.gps.GpsRandomGiftResponse;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.common.UserLevelProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 리팩터링 전 선물상자 보상 동작을 고정하는 특성 테스트. */
@ExtendWith(MockitoExtension.class)
class GiftBoxServiceTest {

    @InjectMocks
    private GiftBoxService giftBoxService;

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private GeoOperations<String, String> geoOperations;
    @Mock private SetOperations<String, String> setOperations;
    @Mock private UserFinder userFinder;
    @Mock private UserItemMapRepository userItemMapRepository;
    @Mock private UserLevelProcessor userLevelProcessor;

    private static final String USER_UUID = "user-1";
    private static final String GIFT_ID = "GIFT_x";
    private static final String ACQUIRED_KEY =
            GpsRedisKeys.USER_ACQUIRED_PREFIX + USER_UUID + ":" + GpsRedisKeys.RANDOM_GIFT_BOX_KEY;
    private static final GpsRandomGiftRequest REQUEST = new GpsRandomGiftRequest(37.5, 127.0);
    private static final List<String> BONUS_TYPES =
            List.of("WOOD_SHOVEL", "SILVER_SHOVEL", "PAPER_BOX", "BRIEFCASE", "GACHA_TICKET");

    private void stubNearest(String... ids) {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        List<GeoResult<GeoLocation<String>>> results = Arrays.stream(ids)
                .map(id -> new GeoResult<>(new GeoLocation<>(id, new Point(127.0, 37.5)),
                        new Distance(10, RedisGeoCommands.DistanceUnit.METERS)))
                .toList();
        when(geoOperations.radius(eq(GpsRedisKeys.RANDOM_GIFT_BOX_KEY), any(Circle.class), any(GeoRadiusCommandArgs.class)))
                .thenReturn(new GeoResults<>(results));
    }

    @Test
    void 반경_50m_안에_선물상자가_없으면_예외가_발생한다() {
        stubNearest();

        assertThatThrownBy(() -> giftBoxService.getRandomGift(REQUEST, USER_UUID)).isInstanceOf(BaseException.class);
        verify(userFinder, never()).getByUuid(anyString());
    }

    @Test
    void 이미_획득한_상자면_예외가_발생한다() {
        stubNearest(GIFT_ID);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(ACQUIRED_KEY, GIFT_ID)).thenReturn(true);

        assertThatThrownBy(() -> giftBoxService.getRandomGift(REQUEST, USER_UUID)).isInstanceOf(BaseException.class);
        verify(userFinder, never()).getByUuid(anyString());
    }

    @Test
    void 상자를_열면_기본_보상_세_종류와_보너스가_유저에게_반영된다() {
        stubNearest(GIFT_ID);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(ACQUIRED_KEY, GIFT_ID)).thenReturn(false);
        UserEntity user = new UserEntity("tester", USER_UUID, 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userFinder.getByUuid(USER_UUID)).thenReturn(user);
        lenient().when(userItemMapRepository.findByUserIdAndItemName(anyLong(), any())).thenReturn(Optional.empty());

        GpsRandomGiftResponse response = giftBoxService.getRandomGift(REQUEST, USER_UUID);

        assertThat(response.randomGiftList()).extracting("name").containsExactly("coin", "vacationTicket", "exp");
        assertThat(user.getCoin()).isBetween(10_000, 80_000);
        assertThat(user.getVacationTicket()).isBetween(3, 200);
        verify(userLevelProcessor).processExpGainAndLevelUp(user, 20);

        List<?> bonusGifts = response.bonusGiftInfo();
        List<String> bonusNames = bonusGifts.stream()
                .map(info -> (String) ReflectionTestUtils.getField(info, "name")).toList();
        assertThat(BONUS_TYPES).containsAll(bonusNames);
        assertThat(bonusGifts).extracting("quantity").allMatch(quantity -> quantity.equals(1));
        long itemBonuses = bonusNames.stream().filter(n -> !n.equals("GACHA_TICKET")).count();
        verify(userItemMapRepository, times((int) itemBonuses)).save(any());
        assertThat(user.getGachaTicket()).isEqualTo(bonusNames.contains("GACHA_TICKET") ? 1 : 0);

        verify(setOperations).add(ACQUIRED_KEY, GIFT_ID);
        verify(redisTemplate).expire(ACQUIRED_KEY, 5L, TimeUnit.DAYS);
    }
}
