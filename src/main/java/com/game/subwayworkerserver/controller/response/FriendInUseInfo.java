package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.FriendLevel;
import com.game.subwayworkerserver.entity.FriendEntity;

import java.util.List;

public record FriendInUseInfo(
    String name,
    String position,
    int level
) {
    public static List<FriendInUseInfo> of(final List<FriendEntity> friendInUse) {
        return  friendInUse.stream()
                .map(friend ->
                        new FriendInUseInfo(
                                friend.getName().getKoreanName(),
                                FriendLevel.of(friend.getLevel()).getTitle(),
                                friend.getLevel()
                        )
                ).toList();
    }
}
