package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.FriendType;

import java.util.List;

public record RandomGpsResponse(
        List<RandomFriendLocationResponse> friends,
        List<ItemLocationResponse> giftBoxes
) {

    public record RandomFriendLocationResponse(
            String friendId,
            String engName,
            String korName,
            int friendLevel,
            String position,
            Double latitude,
            Double longitude,
            Double distanceKm
    ) {

        private final static String EMPTY = "empty";

        public static RandomFriendLocationResponse of(
                final String friendId,
                final String friendName,
                final int friendLevel,
                final String position,
                final double latitude,
                final double longitude,
                final Double distance
        ) {
            return new RandomFriendLocationResponse(
                    friendId,
                    String.valueOf(FriendType.fromName(friendName)),
                    friendName,
                    friendLevel,
                    position,
                    latitude,
                    longitude,
                    distance
            );
        }

        public static RandomFriendLocationResponse empty() {
            return new RandomFriendLocationResponse(EMPTY, EMPTY, EMPTY, 0, EMPTY, 0.0, 0.0,0.0);
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
