package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.FriendEntity;

import java.util.List;

public record FriendInUseInfo(
    String engName,
    String korName,
    String position,
    int level
) {
    public static List<FriendInUseInfo> of(final List<FriendEntity> friendInUse) {
        return  friendInUse.stream()
                .map(friend ->
                        new FriendInUseInfo(
                                friend.getFriendType().name(),
                                friend.getName(),
                                friend.getLevel().getTitle(),
                                friend.getLevel().getLevel()
                        )
                ).toList();
    }
}
