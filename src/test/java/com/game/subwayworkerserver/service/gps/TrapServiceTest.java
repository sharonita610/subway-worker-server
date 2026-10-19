package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserTrapMapEntity;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserTrapMapRepository;
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
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrapServiceTest {

    @InjectMocks
    private TrapService trapService;

    @Mock private UserTrapMapRepository userTrapMapRepository;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private GeoOperations<String, String> geoOperations;
    @Mock private HashOperations<String, Object, Object> hashOperations;
    @Mock private SetOperations<String, String> setOperations;

    private static final long USER_ID = 1L;
    private static final String USER_UUID = "user-uuid-1";
    private static final String COWORKER_ID = "coworker-7";

    private UserTrapMapEntity activeTrap() {
        UserTrapMapEntity trap = new UserTrapMapEntity(
                USER_ID, ItemName.WOOD_SHOVEL, 37.5, 127.0, LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(trap, "id", 10L);
        return trap;
    }

    private UserEntity user() {
        UserEntity user = new UserEntity("tester", USER_UUID, 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private GeoResults<RedisGeoCommands.GeoLocation<String>> nearby(String coworkerId) {
        RedisGeoCommands.GeoLocation<String> location =
                new RedisGeoCommands.GeoLocation<>(coworkerId, new Point(127.0, 37.5));
        return new GeoResults<>(List.of(new GeoResult<>(location, new Distance(3.0))));
    }

    @Test
    void 만료된_트랩은_삭제되고_유효한_트랩이_없으면_0을_반환한다() {
        UserTrapMapEntity expired = new UserTrapMapEntity(
                USER_ID, ItemName.WOOD_SHOVEL, 37.5, 127.0, LocalDateTime.now().minusHours(1));
        when(userTrapMapRepository.findByExpiresAtBeforeAndIsTrappedFalse(any())).thenReturn(List.of(expired));
        when(userTrapMapRepository.findByExpiresAtAfterAndIsTrappedFalse(any())).thenReturn(List.of());

        int result = trapService.checkTrapsForCapture();

        assertThat(result).isZero();
        verify(userTrapMapRepository).deleteAll(List.of(expired));
    }

    @Test
    void 반경_안에_동료가_있으면_트랩을_성공_처리하고_동료_데이터를_백업한다() {
        UserTrapMapEntity trap = activeTrap();
        UserEntity user = user();
        Map<Object, Object> attributes = Map.of("coworkerName", "오크", "coworkerLevel", "3");

        when(userTrapMapRepository.findByExpiresAtBeforeAndIsTrappedFalse(any())).thenReturn(List.of());
        when(userTrapMapRepository.findByExpiresAtAfterAndIsTrappedFalse(any())).thenReturn(List.of(trap));
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(eq(GpsRedisKeys.COWORKER_LOCATIONS_KEY), any(Circle.class), any(RedisGeoCommands.GeoRadiusCommandArgs.class))).thenReturn(nearby(COWORKER_ID));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(GpsRedisKeys.COWORKER_DATA_PREFIX + COWORKER_ID)).thenReturn(attributes);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        int result = trapService.checkTrapsForCapture();

        assertThat(result).isEqualTo(1);
        assertThat(trap.isTrapped()).isTrue();
        assertThat(trap.getCapturedCoworkerId()).isEqualTo(COWORKER_ID);
        assertThat(user.isTrapSuccess()).isTrue();

        String backupKey = GpsRedisKeys.TRAPPED_COWORKER_DATA_PREFIX + COWORKER_ID;
        verify(hashOperations).putAll(backupKey, attributes);
        verify(redisTemplate).expire(backupKey, 7, TimeUnit.DAYS);
        verify(setOperations).add(
                GpsRedisKeys.USER_ACQUIRED_PREFIX + USER_UUID + ":" + GpsRedisKeys.COWORKER_LOCATIONS_KEY, COWORKER_ID);
    }

    @Test
    void 반경_안에_동료가_없으면_트랩은_그대로_남는다() {
        UserTrapMapEntity trap = activeTrap();

        when(userTrapMapRepository.findByExpiresAtBeforeAndIsTrappedFalse(any())).thenReturn(List.of());
        when(userTrapMapRepository.findByExpiresAtAfterAndIsTrappedFalse(any())).thenReturn(List.of(trap));
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class), any(RedisGeoCommands.GeoRadiusCommandArgs.class))).thenReturn(new GeoResults<>(List.of()));

        int result = trapService.checkTrapsForCapture();

        assertThat(result).isZero();
        assertThat(trap.isTrapped()).isFalse();
        verify(hashOperations, never()).putAll(anyString(), any());
        verify(userRepository, never()).findById(any());
    }
}
