package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class StageRewardProcessor{

    public StageRewardInfo giveFirstClearReward(UserEntity user, int requestedLevel) {
        return null;
    }

    public StageRewardInfo giveRepeatClearReward(UserEntity user, int requestedLevel) {

        return null;
    }
}
