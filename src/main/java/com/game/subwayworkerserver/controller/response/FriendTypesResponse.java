package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.FriendType;

import java.util.List;

public record FriendTypesResponse(
        List<FriendTypeInfo> typeNames
) {
    public static FriendTypesResponse from(final List<FriendType> allAttributesAsList) {
        return new FriendTypesResponse(
                allAttributesAsList.stream()
                        .map(friend ->
                                FriendTypeInfo.of(friend.name(), friend.getKoreanName())
                        ).toList()
        );
    }

    private record FriendTypeInfo(String engName, String korName){
        public static FriendTypeInfo of(String engName, String korName) {
            return new FriendTypeInfo(engName, korName);
        }
    }
}
