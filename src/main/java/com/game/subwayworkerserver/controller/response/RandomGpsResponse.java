package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;

import java.util.List;

public record RandomGpsResponse(
        List<RandomCoworkerLocationResponse> coworkerLocations,
        List<ItemLocationResponse> giftBoxes
) {

    public record RandomCoworkerLocationResponse(
            String coworkerId,
            String engName,
            String korName,
            int coworkerLevel,
            String position,
            Double latitude,
            Double longitude,
            Double distanceKm
    ) {

        private final static String EMPTY = "empty";

        public static RandomCoworkerLocationResponse of(
                final String coworkerId,
                final String coworkerKorName,
                final int coworkerLevel,
                final String position,
                final double latitude,
                final double longitude,
                final Double distance
        ) {
            return new RandomCoworkerLocationResponse(
                    coworkerId,
                    String.valueOf(CoworkerType.fromName(coworkerKorName)),
                    coworkerKorName,
                    coworkerLevel,
                    position,
                    latitude,
                    longitude,
                    distance
            );
        }

        public static RandomCoworkerLocationResponse empty() {
            return new RandomCoworkerLocationResponse(EMPTY, EMPTY, EMPTY, 0, EMPTY, 0.0, 0.0,0.0);
        }
    }

    public record ItemLocationResponse(
            String itemName,
            Double latitude,
            Double longitude,
            Double distanceKm
    ) {
    }
}
