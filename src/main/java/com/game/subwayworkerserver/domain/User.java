package com.game.subwayworkerserver.domain;

import com.game.subwayworkerserver.domain.friend.Friend;
import com.game.subwayworkerserver.domain.level.UserLevel;

import java.util.List;

public record User(
        long id,
        String username,
        int cash,
        int coin,
        UserLevel level,
        Backpack backpack,
        Treasure treasure,
        Ranking ranking,
        List<Friend> friendList
) {

}
