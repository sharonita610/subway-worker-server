package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.baseinfo.FriendLevel;
import com.game.subwayworkerserver.domain.baseinfo.FriendType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FriendDataCreator {

    public Map<String, String> createRandomFriendAttribute() {
        FriendType randomFriend = FriendType.getRandomAttribute();
        FriendLevel randomLevel = FriendLevel.getRandomLevel();
        return createData(randomFriend.getKoreanName(), randomLevel.getTitle(), randomLevel.getLevel());
    }

    public Map<String, String> createData(String koreanName, String position, int level) {
        RandomFriendDataInfo friendData = new RandomFriendDataInfo(koreanName, position, level);
        Map<String, String> dataToSave = new HashMap<>();
        dataToSave.put("friendName", friendData.koreanName());
        dataToSave.put("friendLevel", String.valueOf(friendData.level()));
        dataToSave.put("position", friendData.position());
        dataToSave.put("captureAttemptCount", "0");
        return dataToSave;
    }

    private record RandomFriendDataInfo(String koreanName, String position, int level) {
    }
}
