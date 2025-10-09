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
        if (count > allSkills.length) {
            throw new IllegalArgumentException("요청한 패시브 스킬 개수(" + count + ")가 전체 스킬 개수(" + allSkills.length + ")보다 많습니다.");
        }

        // 1. 전체 스킬 리스트를 생성합니다.
        List<PassiveSkillCard> skillList = new ArrayList<>(List.of(allSkills));

        // 2. 리스트를 무작위로 섞습니다.
        Collections.shuffle(skillList, RANDOM);

        // 3. 앞에서부터 요청된 개수만큼 잘라서 반환합니다.
        return skillList.subList(0, count);
    }

    public static List<ActiveSkillCard> getRandomActiveSkills(int count) {
        ActiveSkillCard[] allSkills = ActiveSkillCard.values();
        if (count > allSkills.length) {
            throw new IllegalArgumentException("요청한 액티브 스킬 개수(" + count + ")가 전체 스킬 개수(" + allSkills.length + ")보다 많습니다.");
        }

        // 1. 전체 스킬 리스트를 생성합니다.
        List<ActiveSkillCard> skillList = new ArrayList<>(List.of(allSkills));

        // 2. 리스트를 무작위로 섞습니다.
        Collections.shuffle(skillList, RANDOM);

        // 3. 앞에서부터 요청된 개수만큼 잘라서 반환합니다.
        return skillList.subList(0, count);
    }

    // --- 예시 사용 ---
    public static void main(String[] args) {
        System.out.println("--- 랜덤 패시브 스킬 2개 ---");
        List<PassiveSkillCard> passiveSkills = getRandomPassiveSkills(2);
        passiveSkills.forEach(skill -> System.out.println("ID:" + skill.getId() + ", 이름: " + skill.getDisplayName()));

        System.out.println("\n--- 랜덤 액티브 스킬 2개 ---");
        List<ActiveSkillCard> activeSkills = getRandomActiveSkills(2);
        activeSkills.forEach(skill -> System.out.println("ID:" + skill.getId() + ", 이름: " + skill.getDisplayName()));
    }
}
