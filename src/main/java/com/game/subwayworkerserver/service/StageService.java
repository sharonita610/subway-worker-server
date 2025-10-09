package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.sampleData.MonsterSampleData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StageService {

    public List<MonsterSampleData.MonsterType> getStageMonsters(int num) {
        MonsterSampleData.initializeData();
        List<MonsterSampleData.MonsterType> selectedMonsters = null;

        if(num == 1){
            selectedMonsters = MonsterSampleData.sampleMonster1;
        } else if(num == 2){
            selectedMonsters = MonsterSampleData.sampleMonster2;
        }

        return selectedMonsters;
    }
}
