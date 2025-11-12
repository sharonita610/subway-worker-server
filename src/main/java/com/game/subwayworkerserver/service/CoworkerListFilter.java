package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CoworkerFilterRequest;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                        .toList();
            }

            // 2-2. 스킬 필터링
            List<String> skills = request.skills();
            if (skills != null && !skills.isEmpty()) {
                filteredAndSortedCoworkers = filteredAndSortedCoworkers.stream()
                        .filter(coworker -> coworkerHasAllSkills(coworker, skills))
                        .toList();
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
    private boolean coworkerHasAllSkills(CoworkerOnSale coworker, List<String> requiredSkills) {

        // 1. CoworkerOnSale DTO에서 액티브 및 패시브 스킬 목록을 가져옵니다.
        List<ActiveSkillCard> activeSkills = coworker.activeSkills();
        List<PassiveSkillCard> passiveSkills = coworker.passiveSkills();

        // 2. ⭐ 액티브 스킬 이름을 String 리스트로 변환 ⭐
        // ActiveSkillCard::getKoreanName 메서드가 있다고 가정합니다.
        List<String> activeSkillNames = activeSkills.stream()
                .map(ActiveSkillCard::getKoreanName)
                .toList();

        // 3. ⭐ 패시브 스킬 이름을 String 리스트로 변환 ⭐
        // PassiveSkillCard::getKoreanName 메서드가 있다고 가정합니다.
        List<String> passiveSkillNames = passiveSkills.stream()
                .map(PassiveSkillCard::getKoreanName)
                .toList();

        // 4. ⭐ 두 목록을 하나로 통합 (List<String> coworkerList 생성) ⭐
        List<String> coworkerList = new ArrayList<>(activeSkillNames);
        coworkerList.addAll(passiveSkillNames);

        // 5. requiredSkills의 모든 요소가 통합된 coworkerSkills에 포함되는지 확인
        return coworkerList.containsAll(requiredSkills);
    }

    /**
     * 정렬 기준 문자열에 따라 Comparator를 생성합니다.
     */
    private Comparator<CoworkerOnSale> createCoworkerComparator(String sortType) {
        // ⭐ 모든 Comparator를 coworkerOnSale 타입으로 변경했습니다. ⭐
        return switch (sortType.toLowerCase()) {
            case "가격내림차순" -> Comparator.comparing(CoworkerOnSale::price).reversed();
            case "가격오름차순" -> Comparator.comparing(CoworkerOnSale::price);
            case "레벨높은순" -> Comparator.comparing(CoworkerOnSale::level).reversed();
            case "공격력강한순" -> Comparator.comparing(CoworkerOnSale::attack).reversed();
            case "hp높은순" -> Comparator.comparing(CoworkerOnSale::hp).reversed();
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
                .collect(Collectors.toList());
    }
}