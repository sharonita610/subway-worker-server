package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;

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
                .comparing(SkillCardEntity::getCurrentLevel, Comparator.reverseOrder())
                .thenComparing(SkillCardEntity::getCount, Comparator.reverseOrder())
                .thenComparing(SkillCardEntity::getKorName, Comparator.naturalOrder());
    }

    public List<SkillCardOnSale> filter(List<SkillCardOnSale> cards, SkillCardFilterRequest request) {
        Stream<SkillCardOnSale> stream = cards.stream();

        if (request.levels() != null && !request.levels().isEmpty()) {
            stream = stream.filter(card -> request.levels().contains(card.currentLevel()));
        }

        List<String> combinedSkills = getCombinedSkills(request);
        if (!combinedSkills.isEmpty()) {
            stream = stream.filter(card -> combinedSkills.contains(card.engName()));
        }

        stream = stream.sorted(getComparator(request.sortType()));

        return stream.toList();
    }

    private Comparator<SkillCardOnSale> getComparator(String sortType) {
        if (sortType == null) {
            return Comparator.comparing(SkillCardOnSale::currentLevel).reversed();
        }

        SkillSortType type = SkillSortType.fromKoreanName(sortType);

        return switch (type) {
            case PRICE_ASC -> Comparator.comparing(SkillCardOnSale::price);
            case LEVEL_DESC -> Comparator.comparing(SkillCardOnSale::currentLevel).reversed();
            case PRICE_DESC, DEFAULT -> Comparator.comparing(SkillCardOnSale::price).reversed();
        };
    }

    private List<String> getCombinedSkills(SkillCardFilterRequest request) {
        List<String> combined = new java.util.ArrayList<>();
        if (request.passiveSkills() != null) {
            combined.addAll(request.passiveSkills());
        }
        if (request.activeSkills() != null) {
            combined.addAll(request.activeSkills());
        }
        return combined;
    }
}