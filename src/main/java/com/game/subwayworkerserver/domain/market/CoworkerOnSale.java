package com.game.subwayworkerserver.domain.market;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.CoworkerMarketEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record CoworkerOnSale(
        long marketId,
        long coworkerId,
        String engName,
        String korName,
        boolean inUse,
        boolean onSale,
        int level,
        int exp,
        int totalExp,
        String position,
        String element,
        long hp,
        long attack,
        long defense,
        long mana,
        long maxMana,
        long manaRecovery,
        int activeSkillId1,
        int activeSkillId2,
        int passiveSkillId1,
        int passiveSkillId2,
        List<ActiveSkillCard> activeSkills,
        List<PassiveSkillCard> passiveSkills,
        int price,
        long sellerId,
        boolean isPaid,
        boolean isSold
) {
    // 2. 정적 팩토리 메서드 구현
    public static List<CoworkerOnSale> toDomain(final List<CoworkerMarketEntity> marketCoworkers, final List<CoworkerEntity> allCoworkersOnSale) {

        Map<Long, CoworkerEntity> coworkerMap = allCoworkersOnSale.stream()
                .collect(Collectors.toMap(
                        CoworkerEntity::getId,
                        coworker -> coworker
                ));

        return marketCoworkers.stream()
                .map(market -> {
                    long coworkerId = market.getCoworkerId();
                    CoworkerEntity coworker = coworkerMap.get(coworkerId);

                    // 데이터가 누락되었거나 필터링 조건에 의해 제거된 경우 (null일 경우) 스킵
                    if (coworker == null) {
                        return null;
                    }

                    return CoworkerOnSale.builder()
                            // MarketEntity (마켓 정보)
                            .marketId(market.getId())
                            .coworkerId(coworkerId)
                            .price(market.getPrice())
                            .sellerId(market.getSellerId())
                            .isPaid(market.isPaid())
                            .isSold(market.isSold())
                            .engName(coworker.getCoworkerType().name())
                            .korName(coworker.getName())
                            .level(coworker.getLevel().getLevel())
                            .exp(coworker.getCurrentExp())
                            .totalExp(coworker.getTotalExp())
                            .position(coworker.getLevel().getTitle())
                            .inUse(coworker.isInUse())
                            .onSale(coworker.isOnSale())
                            .element(coworker.getCoworkerElement().getKoreanName())
                            .hp(coworker.getHp())
                            .attack(coworker.getAttack())
                            .defense(coworker.getDefense())
                            .mana(coworker.getMana())
                            .maxMana(coworker.getMaxMana())
                            .manaRecovery(coworker.getManaRecovery())
                            .activeSkillId1(coworker.getActiveSkill1())
                            .activeSkillId2(coworker.getActiveSkill2())
                            .passiveSkillId1(coworker.getPassiveSkill1())
                            .passiveSkillId2(coworker.getPassiveSkill2())
                            .activeSkills(coworker.getActiveSkills())
                            .passiveSkills(coworker.getPassiveSkills())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}