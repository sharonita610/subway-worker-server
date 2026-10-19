package com.game.subwayworkerserver.service.gps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GpsItemsCreatorTest {

    @InjectMocks
    private GpsItemsCreator creator;

    @Mock private CoworkerDataCreator coworkerDataCreator;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private SetOperations<String, String> setOperations;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private GeoOperations<String, String> geoOperations;
    @Mock private HashOperations<String, Object, Object> hashOperations;

    @Test
    void 이미_방문한_grid에서는_아무것도_생성하지_않는다() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(eq(GpsRedisKeys.VISITED_GRID_KEY), anyString())).thenReturn(true);

        creator.createItemsInArea(37.5, 127.0);

        verify(redisTemplate, never()).opsForValue();
        verify(redisTemplate, never()).opsForGeo();
        verify(redisTemplate, never()).expire(anyString(), eq(1L), eq(TimeUnit.HOURS));
    }

    @SuppressWarnings("unchecked")
    @Test
    void 방문하지_않은_grid_9칸마다_동료_2명과_선물상자_2개를_생성하고_락을_해제한다() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(eq(GpsRedisKeys.VISITED_GRID_KEY), anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(startsWith(GpsRedisKeys.GRID_LOCK_PREFIX), eq("LOCKED"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(coworkerDataCreator.createRandomCoworkerAttribute())
                .thenAnswer(inv -> new HashMap<>(Map.of("coworkerName", "뭉실", "coworkerLevel", "1", "position", "공격수")));

        creator.createItemsInArea(37.5, 127.0);

        ArgumentCaptor<Map<String, Point>> coworkerBatches = ArgumentCaptor.forClass(Map.class);
        verify(geoOperations, times(9)).add(eq(GpsRedisKeys.COWORKER_LOCATIONS_KEY), coworkerBatches.capture());
        assertThat(coworkerBatches.getAllValues()).allSatisfy(batch -> assertThat(batch).hasSize(2));
        verify(geoOperations, times(9)).add(eq(GpsRedisKeys.RANDOM_GIFT_BOX_KEY), anyMap());
        verify(hashOperations, times(18)).putAll(startsWith(GpsRedisKeys.COWORKER_DATA_PREFIX), anyMap());
        verify(setOperations, times(9)).add(eq(GpsRedisKeys.VISITED_GRID_KEY), anyString());
        verify(redisTemplate, times(9)).delete(startsWith(GpsRedisKeys.GRID_LOCK_PREFIX));
        verify(redisTemplate).expire(GpsRedisKeys.COWORKER_LOCATIONS_KEY, 1L, TimeUnit.HOURS);
        verify(redisTemplate).expire(GpsRedisKeys.RANDOM_GIFT_BOX_KEY, 1L, TimeUnit.HOURS);
    }

    @Test
    void 락을_얻지_못한_grid는_건너뛴다() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(eq(GpsRedisKeys.VISITED_GRID_KEY), anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(false);

        creator.createItemsInArea(37.5, 127.0);

        verify(redisTemplate, never()).opsForGeo();
        verify(redisTemplate, never()).delete(anyString());
    }
}
