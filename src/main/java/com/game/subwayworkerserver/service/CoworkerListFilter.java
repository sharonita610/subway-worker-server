package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CoworkerFilterRequest;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
public class CoworkerListFilter {

    public List<CoworkerOnSale> filter(List<CoworkerOnSale> coworkerOnSaleList, CoworkerFilterRequest request) {
        List<CoworkerOnSale> filteredAndSortedCoworkers = new ArrayList<>(coworkerOnSaleList);

        if (request != null) {
            // 2-1. 속성 필터링
            List<String> attributes = request.elements();
            if (attributes != null && !attributes.isEmpty()) {
                filteredAndSortedCoworkers = filteredAndSortedCoworkers.stream()
                        .filter(coworker -> coworkerHasAnyElement(coworker, attributes))
                        .collect(toList());
            }

            // 2-2. 스킬 필터링
            List<String> skills = request.skills();
            if (skills != null && !skills.isEmpty()) {
                filteredAndSortedCoworkers = filteredAndSortedCoworkers.stream()
                        .filter(coworker -> coworkerHasAllSkills(coworker, skills))
                        .collect(toList());
            }

            // 3. 정렬 (req가 null이 아닐 때만 정렬 조건 적용)
            String sortType = request.sortType();
            if (sortType != null && !sortType.isBlank()) {
                // ⭐ Comparator의 타입을 coworkerOnSale로 변경 ⭐
                Comparator<CoworkerOnSale> comparator = createCoworkerComparator(sortType);
                filteredAndSortedCoworkers.sort(comparator);
            }
        }

        // 4. req가 null이거나 sortType이 없으면, 기본 정렬(레벨 높은 순)을 적용합니다.
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
     * 동료가 요청된 모든 스킬을 가지고 있는지 확인합니다.
     */
    // CoworkerListFilter.java

    /**
     * 동료가 요청된 모든 스킬을 가지고 있는지 확인합니다. (HashSet으로 최적화)
     */
    private boolean coworkerHasAllSkills(CoworkerOnSale coworker, List<String> requiredSkills) {


        List<Integer> userActiveSkills = coworker.getUserDefaultActiveSkills();
        List<Integer> userPassiveSkills = coworker.getUserDefaultPassiveSkills();

        // 1. 모든 스킬 이름을 통합하여 List에 모읍니다.
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
     * 정렬 기준 문자열에 따라 Comparator를 생성합니다.ㅁ
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

        // 1. Comparator 정의 (우선순위 순서대로 thenComparing 연결)
        Comparator<CoworkerEntity> coworkerComparator = Comparator

                // 1순위: 파트너 먼저 (true가 false보다 먼저 오도록)
                // (isPartner()가 boolean이면, Boolean.compare(b2, b1) 또는 b1.compareTo(b2)를 반대로 사용)
                .comparing(CoworkerEntity::isInUse, Comparator.reverseOrder())

                // 2순위: 레벨 높은 순
                .thenComparing(coworkerEntity -> coworkerEntity.getLevel().getLevel(), Comparator.reverseOrder())

                // 3순위: 같은 레벨이면 경험치 높은 순
                .thenComparing(CoworkerEntity::getCurrentExp, Comparator.reverseOrder())

                // 4순위: 공격력 높은 순
                .thenComparing(CoworkerEntity::getAttack, Comparator.reverseOrder())

                // 5순위: 체력 높은 순
                .thenComparing(CoworkerEntity::getHp, Comparator.reverseOrder());

        // 2. Stream을 사용하여 정렬 및 새로운 리스트 반환
        return coworkerEntityList.stream()
                .sorted(coworkerComparator)
                .collect(toList());
    }
}