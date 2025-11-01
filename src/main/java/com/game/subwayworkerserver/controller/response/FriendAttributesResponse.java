package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.FriendAttribute;

import java.util.List;

public record FriendAttributesResponse(
        List<FriendAttributeInfo> attributeNames
) {
    public static FriendAttributesResponse from(final List<FriendAttribute> allAttributesAsList) {
        return new FriendAttributesResponse(
                allAttributesAsList.stream()
                        .map(friend ->
                                FriendAttributeInfo.of(String.valueOf(friend), friend.getKoreanName())
                        ).toList()
        );
    }

    private record FriendAttributeInfo(String engName, String korName){
        public static FriendAttributeInfo of(String engName, String korName) {
            return new FriendAttributeInfo(engName, korName);
        }
    }
}
