package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CoworkerDataCreator {

    public Map<String, String> createRandomCoworkerAttribute() {
        CoworkerType randomCoworker = CoworkerType.getRandomAttribute();
        CoworkerLevel randomLevel = CoworkerLevel.getRandomLevel();
        return createData(randomCoworker.getKoreanName(), randomLevel.getTitle(), randomLevel.getLevel());
    }

    public Map<String, String> createData(String koreanName, String position, int level) {
        RandomCoworkerDataInfo coworkerData = new RandomCoworkerDataInfo(koreanName, position, level);
        Map<String, String> dataToSave = new HashMap<>();
        dataToSave.put("coworkerName", coworkerData.koreanName());
        dataToSave.put("coworkerLevel", String.valueOf(coworkerData.level()));
        dataToSave.put("position", coworkerData.position());
        dataToSave.put("captureAttemptCount", "0");
        return dataToSave;
    }

    private record RandomCoworkerDataInfo(String koreanName, String position, int level) {
    }
}
