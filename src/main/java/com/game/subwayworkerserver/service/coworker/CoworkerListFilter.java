package com.game.subwayworkerserver.service.coworker;

import com.game.subwayworkerserver.controller.request.market.CoworkerFilterRequest;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
public class CoworkerListFilter {

    public List<CoworkerOnSale> filter(List<CoworkerOnSale> coworkerOnSaleList, CoworkerFilterRequest request) {
        List<CoworkerOnSale> filteredAndSortedCoworkers = new ArrayList<>(coworkerOnSaleList);

        if (request != null) {
            List<String> attributes = request.elements();
            if (attributes != null && !attributes.isEmpty()) {
                filteredAndSortedCoworkers = filteredAndSortedCoworkers.stream()
                        .filter(coworker -> coworkerHasAnyElement(coworker, attributes))
                        .collect(toList());
            }

            List<String> skills = request.skills();
            if (skills != null && !skills.isEmpty()) {
                filteredAndSortedCoworkers = filteredAndSortedCoworkers.stream()
                        .filter(coworker -> coworkerHasAllSkills(coworker, skills))
                        .collect(toList());
            }

            String sortType = request.sortType();
            if (sortType != null && !sortType.isBlank()) {
                Comparator<CoworkerOnSale> comparator = createCoworkerComparator(sortType);
                filteredAndSortedCoworkers.sort(comparator);
            }
        }

        if (request == null || request.sortType() == null || request.sortType().isBlank()) {
            filteredAndSortedCoworkers.sort(Comparator.comparing(CoworkerOnSale::level).reversed());
        }

        return filteredAndSortedCoworkers;
    }

    private boolean coworkerHasAnyElement(CoworkerOnSale coworker, List<String> requiredAttributes) {
         String coworkerElement = coworker.engElement();
        return requiredAttributes.contains(coworkerElement);
    }

    /**
     * 동료가 요청된 모든 스킬을 가지고 있는지 확인합니다. (HashSet으로 최적화)
     */
    private boolean coworkerHasAllSkills(CoworkerOnSale coworker, List<String> requiredSkills) {

        List<Integer> userActiveSkills = coworker.getUserDefaultActiveSkills();
        List<Integer> userPassiveSkills = coworker.getUserDefaultPassiveSkills();

        List<String> activeSkillNames = userActiveSkills.stream()
                .map(skill -> ActiveSkillCard.from(skill).name()).toList();
        List<String> passiveSkillNames = userPassiveSkills.stream()
                .map(skill -> PassiveSkillCard.from(skill).name()).toList();
        List<String> combinedList = new ArrayList<>(activeSkillNames);
        combinedList.addAll(passiveSkillNames);

        Set<String> coworkerSkills = new HashSet<>(combinedList);

        return requiredSkills.stream().anyMatch(coworkerSkills::contains);
    }

    /**
     * 정렬 기준 문자열에 따라 Comparator를 생성합니다.
     */
    private Comparator<CoworkerOnSale> createCoworkerComparator(String sortType) {
        if (sortType == null) {
            return Comparator.comparing(CoworkerOnSale::level).reversed();
        }
        return switch (sortType) {
            case "가격낮은순" -> Comparator.comparing(CoworkerOnSale::price);
            case "가격높은순" -> Comparator.comparing(CoworkerOnSale::price).reversed();
            case "공격력높은순" -> Comparator.comparing(CoworkerOnSale::attack).reversed();
            case "HP높은순" -> Comparator.comparing(CoworkerOnSale::hp).reversed();
            case "방어력높은순" -> Comparator.comparing(CoworkerOnSale::defense).reversed();
            default -> Comparator.comparing(CoworkerOnSale::level).reversed();
        };
    }

    public List<CoworkerEntity> sort(List<CoworkerEntity> coworkerEntityList) {

        Comparator<CoworkerEntity> coworkerComparator = Comparator

                // 1순위: 파트너 먼저 (true가 false보다 먼저 오도록)
                .comparing(CoworkerEntity::isInUse, Comparator.reverseOrder())

                // 2순위: 레벨 높은 순
                .thenComparing(coworkerEntity -> coworkerEntity.getLevel().getLevel(), Comparator.reverseOrder())

                // 3순위: 같은 레벨이면 경험치 높은 순
                .thenComparing(CoworkerEntity::getCurrentExp, Comparator.reverseOrder())

                // 4순위: 공격력 높은 순
                .thenComparing(CoworkerEntity::getAttack, Comparator.reverseOrder())

                // 5순위: 체력 높은 순
                .thenComparing(CoworkerEntity::getHp, Comparator.reverseOrder());

        return coworkerEntityList.stream()
                .sorted(coworkerComparator)
                .collect(toList());
    }
}