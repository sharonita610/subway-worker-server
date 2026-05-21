package com.game.subwayworkerserver.util;


import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkillRandomizer {

    private static final Random RANDOM = new Random();

    public static List<PassiveSkillCard> getRandomPassiveSkills(int count) {
        PassiveSkillCard[] allSkills = PassiveSkillCard.values();

        List<PassiveSkillCard> skillList = new ArrayList<>(List.of(allSkills));
        skillList.remove(PassiveSkillCard.NONE); // ⭐ NONE 카드 제거 ⭐
        int totalAvailable = skillList.size(); // 사용 가능한 스킬의 실제 개수
        if (count > totalAvailable) {
            throw new IllegalArgumentException("요청한 패시브 스킬 개수(" + count + ")가 전체 스킬 개수(" + allSkills.length + ")보다 많습니다.");
        }

        // 2. 리스트를 무작위로 섞습니다.
        Collections.shuffle(skillList, RANDOM);

        // 3. 앞에서부터 요청된 개수만큼 잘라서 반환합니다.
        return skillList.subList(0, count);
    }

    public static List<ActiveSkillCard> getRandomActiveSkills(int count) {
        ActiveSkillCard[] allSkills = ActiveSkillCard.values();
        List<ActiveSkillCard> skillList = new ArrayList<>(List.of(allSkills));
        skillList.remove(ActiveSkillCard.NONE); // ⭐ NONE 카드 제거 ⭐

        int totalAvailable = skillList.size(); // 사용 가능한 스킬의 실제 개수

        if (count > totalAvailable) {
            throw new IllegalArgumentException("요청한 액티브 스킬 개수(" + count + ")가 전체 스킬 개수(" + totalAvailable + ")보다 많습니다.");
        }

        // 2. 리스트를 무작위로 섞습니다.
        Collections.shuffle(skillList, RANDOM);

        // 3. 앞에서부터 요청된 개수만큼 잘라서 반환합니다.
        return skillList.subList(0, count);
    }

}
