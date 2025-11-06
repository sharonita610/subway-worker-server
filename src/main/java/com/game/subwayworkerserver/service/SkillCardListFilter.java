package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

@Component
public class SkillCardListFilter {

    public List<SkillCardEntity> defaultFilter(List<SkillCardEntity> skillCardEntityList) {

        List<SkillCardEntity> sortedCards = new ArrayList<>(skillCardEntityList);
        Comparator<SkillCardEntity> defaultComparator = createDefaultEntityComparator();
        sortedCards.sort(defaultComparator);

        return sortedCards;
    }

    private Comparator<SkillCardEntity> createDefaultEntityComparator() {
        return Comparator
                // 1순위: 현재 레벨 높은 순 (내림차순)
                .comparing(SkillCardEntity::getCurrentLevel, Comparator.reverseOrder())
                // 2순위: 같은 레벨이라면 현재 보유 수량(quantity) 많은 순 (내림차순)
                .thenComparing(SkillCardEntity::getCount, Comparator.reverseOrder())
                // 3순위: 같은 수량이라면 한글 이름 가나다 순 (오름차순)
                .thenComparing(SkillCardEntity::getKorName);
    }
}