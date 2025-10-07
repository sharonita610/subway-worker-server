package com.game.subwayworkerserver.domain.friend;

import com.game.subwayworkerserver.domain.skill.Skill;

public record Friend(
        long id,
        long hp,
        long mana,
        long attack,
        long defense,
        boolean inUse,
        long currentExp,
        boolean onSale,
        FriendLevel level,
        Skill skill
) {



}

