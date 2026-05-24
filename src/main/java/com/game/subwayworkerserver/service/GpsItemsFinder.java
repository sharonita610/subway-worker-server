package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpsItemsFinder {

    private static final String COWORKER_LOCATIONS_KEY = GpsRedisKeys.COWORKER_LOCATIONS_KEY;
    private static final String RANDOM_GIFT_BOX_KEY = GpsRedisKeys.RANDOM_GIFT_BOX_KEY;
    private static final String USER_ACQUIRED_PREFIX = GpsRedisKeys.USER_ACQUIRED_PREFIX;
    private static final String COWORKER_DATA_PREFIX = GpsRedisKeys.COWORKER_DATA_PREFIX;

    private final RedisTemplate<String, String> redisTemplate;

    public RandomGpsResponse findItemsInArea(final double latitude, double longitude, final String userUuid) {
        var searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(1, RedisGeoCommands.DistanceUnit.KILOMETERS)
        );

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sort(Sort.Direction.ASC);

        CompletableFuture<List<RandomGpsResponse.RandomCoworkerLocationResponse>> coworkersFuture =
                CompletableFuture.supplyAsync(() ->
                        findAndFilter(COWORKER_LOCATIONS_KEY, searchArea, args, userUuid, this::mapToCoworkerResponse));

        CompletableFuture<List<RandomGpsResponse.ItemLocationResponse>> giftBoxesFuture =
                CompletableFuture.supplyAsync(() ->
                        findAndFilter(RANDOM_GIFT_BOX_KEY, searchArea, args, userUuid, this::mapToItemResponse));

        return new RandomGpsResponse(coworkersFuture.join(), giftBoxesFuture.join());
    }

    private <T> List<T> findAndFilter(
            String geoKey,
            Circle area,
            RedisGeoCommands.GeoRadiusCommandArgs args,
            String userUuid,
            Function<GeoResult<RedisGeoCommands.GeoLocation<String>>, T> mapper
    ) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(geoKey, area, args);

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> acquiredIds = getAcquiredIds(userUuid, geoKey);

        return results.getContent().stream()
                .filter(result -> !acquiredIds.contains(result.getContent().getName()))
                .map(mapper)
                .toList();
    }

    private RandomGpsResponse.RandomCoworkerLocationResponse mapToCoworkerResponse(GeoResult<RedisGeoCommands.GeoLocation<String>> result) {
        String coworkerId = result.getContent().getName();
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(COWORKER_DATA_PREFIX + coworkerId);

        if (hashData.isEmpty()) {
            log.warn("Missing data for coworker ID: {}", coworkerId);
            return RandomGpsResponse.RandomCoworkerLocationResponse.empty();
        }

        return RandomGpsResponse.RandomCoworkerLocationResponse.of(
                coworkerId,
                (String) hashData.get("coworkerName"),
                Integer.parseInt((String) hashData.get("coworkerLevel")),
                (String) hashData.get("position"),
                result.getContent().getPoint().getY(),
                result.getContent().getPoint().getX(),
                result.getDistance().getValue()
        );
    }

    private RandomGpsResponse.ItemLocationResponse mapToItemResponse(GeoResult<RedisGeoCommands.GeoLocation<String>> result) {
        return new RandomGpsResponse.ItemLocationResponse(
                "giftbox",
                result.getContent().getPoint().getY(),
                result.getContent().getPoint().getX(),
                result.getDistance().getValue()
        );
    }

    private Set<String> getAcquiredIds(String userUuid, String geoKey) {
        String acquiredKey = USER_ACQUIRED_PREFIX + userUuid + ":" + geoKey;
        Set<String> acquiredIds = redisTemplate.opsForSet().members(acquiredKey);
        return (acquiredIds != null) ? acquiredIds : Collections.emptySet();
    }
}