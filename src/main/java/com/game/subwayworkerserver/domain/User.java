package com.game.subwayworkerserver.domain;

import com.game.subwayworkerserver.domain.level.UserLevel;

import java.util.List;

public record User(
        long id,
        String username,
        UserLevel level,
        Backpack backpack,
        Treasure treasure,
        Ranking ranking,
        List<Friend> friendList
) {

}
