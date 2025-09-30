package com.game.subwayworkerserver.domain;

public record User(
        String username,
        long level,
        long experience,
        Backpack backpack,
        Treasure treasure,
        Ranking ranking
) {

}
