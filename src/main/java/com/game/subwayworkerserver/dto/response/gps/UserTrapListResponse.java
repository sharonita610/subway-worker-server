package com.game.subwayworkerserver.dto.response.gps;

import com.game.subwayworkerserver.entity.UserTrapMapEntity;

import java.time.LocalDateTime;
import java.util.List;

public record UserTrapListResponse(
        List<UserTrapBaseInfo> trapList
) {

    public static UserTrapListResponse of(List<UserTrapMapEntity> userTrapMapList) {
        return new UserTrapListResponse(
                userTrapMapList.stream()
                        .map(trap -> UserTrapBaseInfo.of(
                                String.valueOf(trap.getTrapItemName()),
                                trap.getTrapItemName().getName(),
                                trap.getLatitude(),
                                trap.getLongitude(),
                                trap.getExpiresAt())
                        ).toList()
        );
    }

    private record UserTrapBaseInfo(
            String engName,
            String korName,
            double latitude,
            double longitude,
            LocalDateTime expireTime
    ) {

        public static UserTrapBaseInfo of(
                final String engName, final String korName, final double latitude, final double longitude, final LocalDateTime expiresAt
        ) {
            return new UserTrapBaseInfo(engName, korName, latitude, longitude, expiresAt);
        }
    }
}
