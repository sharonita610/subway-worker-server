package com.game.subwayworkerserver.domain.baseinfo;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum ActiveSkillCard {

    NONE(0, "", "", SkillCardType.ACTIVE, 0, 0, 0,
            "", "", false),

    // 201 피로회복제 복용 (내 HP 회복)
    FATIGUE_RELIEVER(201, "피로회복제 복용", "내 HP의 20%를 회복합니다. (필요 마나: 30)",
            SkillCardType.ACTIVE, 0.20, 0.05, 30, "내 턴", "hp", true),

    // 202 헌혈 (내 HP 깎고 아군 회복)
    BLOOD_DONATION(202, "헌혈", "내 HP를 깎고, 선택한 캐릭터의 HP를 20% 회복합니다. (필요 마나: 35)",
            SkillCardType.ACTIVE, 0.20, 0.05, 35, "내 턴", "hp", true),

    // 203 명예수방관 (아군 방어력 증가)
    ARMOR_GUARD(203, "명예소방관", "동료에게 방어력의 150% 만큼의 갑옷을 입힙니다. (필요 마나: 35)",
            SkillCardType.ACTIVE, 1.50, 0.10, 35, "내 턴", "defense", true), // 150% -> 1.50, 10p 증가 -> 0.10

    // 204 갑옷 (내 방어력 버프)
    ARMOR(204, "갑옷", "내 방어력의 150% 만큼의 갑옷을 입습니다. (필요 마나: 25)",
            SkillCardType.ACTIVE, 1.50, 0.10, 25, "상대 턴", "defense", true),

    // 205 CPR (죽은 아군 부활)
    CPR(205, "CPR", "죽은 캐릭터를 HP 20%로 부활시킵니다. (필요 마나: 50)",
            SkillCardType.ACTIVE, 0.20, 0.05, 50, "내 턴", "hp", true),

    // 206 회심의 일격 (단일 대상 공격력 증가)
    CRITICAL_HIT(206, "회심의 일격", "내 공격력의 1.8배로 대상에게 크게 공격합니다. (필요 마나: 50)",
            SkillCardType.ACTIVE, 1.80, 0.10, 50, "내 턴", "attack", true), // 1.8배 -> 1.80, 10p 증가 -> 0.10

    // 207 독가스 살포 (전체 대상 공격)
    POISON_GAS(207, "독가스 살포", "상대 몬스터 전체에 광역 공격을 가합니다. (필요 마나: 40)",
            SkillCardType.ACTIVE, 1.20, 0.10, 40, "내 턴", "attack", true); // 120% -> 1.20, 10% 증가 -> 0.10


    private final int id;
    private final String koreanName;
    private final String description;
    private final SkillCardType type;

    // ⭐ 추가된 속성 ⭐
    private final double level1EffectRate; // 1레벨 효과 수치 (예: 0.20, 1.80)
    private final double diffForLevel; // 레벨당 증가량 (예: 0.05)
    private final int manaCost;          // 필요 마나
    private final String timing;         // 계산 시간 (예: 내 턴, 상대 턴)
    private final String property;
    private final boolean isIncrease;


    ActiveSkillCard(int id, String koreanName, String description, SkillCardType type,
                    double level1EffectRate, double diffForLevel, int manaCost, String timing, String property,
                    boolean isIncrease
    ) {
        this.id = id;
        this.koreanName = koreanName;
        this.description = description;
        this.type = type;
        this.level1EffectRate = level1EffectRate;
        this.diffForLevel = diffForLevel;
        this.manaCost = manaCost;
        this.timing = timing;
        this.property = property;
        this.isIncrease = isIncrease;
    }

    public static ActiveSkillCard from(int id) {
        for (ActiveSkillCard card : values()) {
            if (card.id == id) {
                return card;
            }
        }
        throw new BaseException(ErrorCode.NOT_VALID_ID, "유효하지 않은 Active Skill ID입니다: " + id);
    }

    public static List<ActiveSkillCard> getRandomCard(final int count) {
        List<ActiveSkillCard> availableCards = Stream.of(ActiveSkillCard.values())
                .filter(card -> card != ActiveSkillCard.NONE)
                .toList();

        int totalAvailable = availableCards.size();
        if (count <= 0 || totalAvailable == 0) {
            return Collections.emptyList();
        }

        List<ActiveSkillCard> resultCards = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(totalAvailable);
            resultCards.add(availableCards.get(randomIndex));
        }
        return resultCards;
    }

    /**
     * 특정 레벨에서의 최종 효과 수치를 계산합니다.
     *
     * @param skillLevel 현재 스킬 레벨 (1부터 시작)
     * @return 계산된 최종 효과 수치
     */
    public double getFinalEffectValue(int skillLevel) {

        double diff = this.diffForLevel * skillLevel - 1;
        double effectRate = this.isIncrease
                ? (this.level1EffectRate + diff)
                : (this.level1EffectRate - diff);

        return Math.round(effectRate);
    }
}