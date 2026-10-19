package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse;
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
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GpsItemsFinderTest {

    @InjectMocks
    private GpsItemsFinder finder;

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private GeoOperations<String, String> geoOperations;
    @Mock private HashOperations<String, Object, Object> hashOperations;
    @Mock private SetOperations<String, String> setOperations;

    private static final String USER_UUID = "user-1";
    private static final Map<Object, Object> LIVE_DATA =
            Map.of("coworkerName", "뭉실", "coworkerLevel", "3", "position", "공격수");

    private GeoResult<GeoLocation<String>> at(String id) {
        return new GeoResult<>(new GeoLocation<>(id, new Point(127.0, 37.5)),
                new Distance(0.2, RedisGeoCommands.DistanceUnit.KILOMETERS));
    }

    @SafeVarargs
    private void stubRadius(String key, GeoResult<GeoLocation<String>>... results) {
        when(geoOperations.radius(eq(key), any(Circle.class), any(GeoRadiusCommandArgs.class)))
                .thenReturn(new GeoResults<>(List.of(results)));
    }

    @Test
    void 데이터가_만료된_동료는_응답에서_빠지고_위치_목록에서_제거된다() {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        stubRadius(GpsRedisKeys.COWORKER_LOCATIONS_KEY, at("COWORKER_live"), at("COWORKER_ghost"));
        stubRadius(GpsRedisKeys.RANDOM_GIFT_BOX_KEY);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(Set.of());
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(GpsRedisKeys.COWORKER_DATA_PREFIX + "COWORKER_live")).thenReturn(LIVE_DATA);
        when(hashOperations.entries(GpsRedisKeys.COWORKER_DATA_PREFIX + "COWORKER_ghost")).thenReturn(Map.of());

        RandomGpsResponse response = finder.findItemsInArea(37.5, 127.0, USER_UUID);

        assertThat(response.coworkerLocations()).extracting("coworkerId").containsExactly("COWORKER_live");
        assertThat(response.giftBoxes()).isEmpty();
        verify(geoOperations).remove(GpsRedisKeys.COWORKER_LOCATIONS_KEY, "COWORKER_ghost");
    }

    @Test
    void 이미_획득한_동료는_응답에서_제외된다() {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        stubRadius(GpsRedisKeys.COWORKER_LOCATIONS_KEY, at("COWORKER_a"), at("COWORKER_b"));
        stubRadius(GpsRedisKeys.RANDOM_GIFT_BOX_KEY, at("GIFT_x"));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(GpsRedisKeys.USER_ACQUIRED_PREFIX + USER_UUID + ":" + GpsRedisKeys.COWORKER_LOCATIONS_KEY))
                .thenReturn(Set.of("COWORKER_a"));
        when(setOperations.members(GpsRedisKeys.USER_ACQUIRED_PREFIX + USER_UUID + ":" + GpsRedisKeys.RANDOM_GIFT_BOX_KEY))
                .thenReturn(Set.of());
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(GpsRedisKeys.COWORKER_DATA_PREFIX + "COWORKER_b")).thenReturn(LIVE_DATA);

        RandomGpsResponse response = finder.findItemsInArea(37.5, 127.0, USER_UUID);

        assertThat(response.coworkerLocations()).extracting("coworkerId").containsExactly("COWORKER_b");
        assertThat(response.giftBoxes()).hasSize(1);
        verify(geoOperations, never()).remove(anyString(), any(String[].class));
    }

    @Test
    void 반경_안에_아무것도_없으면_빈_응답을_돌려준다() {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        stubRadius(GpsRedisKeys.COWORKER_LOCATIONS_KEY);
        stubRadius(GpsRedisKeys.RANDOM_GIFT_BOX_KEY);

        RandomGpsResponse response = finder.findItemsInArea(37.5, 127.0, USER_UUID);

        assertThat(response.coworkerLocations()).isEmpty();
        assertThat(response.giftBoxes()).isEmpty();
        verify(redisTemplate, never()).opsForHash();
    }
}
