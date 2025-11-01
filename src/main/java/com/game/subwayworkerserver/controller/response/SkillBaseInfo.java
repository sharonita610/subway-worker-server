package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import lombok.Builder;

import java.util.List;
import java.util.stream.Stream;

public record SkillBaseInfo(
        long id,
        String engName,
        String korName,
        String description,
        int level
) {

    @Builder
    public SkillBaseInfo {
    }

    private static SkillBaseInfo toBaseInfo(long id, String engName, String korName, String description) {
        return SkillBaseInfo.builder()
                .id(id)
                .engName(engName)
                .korName(korName)
                .description(description)
                .build();
    }

    public static List<SkillBaseInfo> activeSkill(int activeSkill1, int activeSkill2) {
        ActiveSkillCard firstSkill = ActiveSkillCard.from(activeSkill1);
        ActiveSkillCard secondSkill = ActiveSkillCard.from(activeSkill2);

        return Stream.of(firstSkill, secondSkill)
                .map(skillCard -> toBaseInfo(
                        skillCard.getId(),
                        skillCard.name(),
                        skillCard.getKoreanName(),
                        skillCard.getDescription()
                )).toList();
    }

    public static List<SkillBaseInfo> passiveSkill(int passiveSkill1, int passiveSkill2) {
        PassiveSkillCard firstSkill = PassiveSkillCard.from(passiveSkill1);
        PassiveSkillCard secondSkill = PassiveSkillCard.from(passiveSkill2);
        return Stream.of(firstSkill, secondSkill)
                .map(skillCard -> toBaseInfo(
                        skillCard.getId(),
                        skillCard.name(),
                        skillCard.getKoreanName(),
                        skillCard.getDescription()
                )).toList();
    }
}
