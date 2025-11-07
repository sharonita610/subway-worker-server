package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Getter
@Entity
@Table(name = "skill_card")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillCardEntity extends BaseTimeEntity {

    private static final int SKILL_MAX_LEVEL = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long coworkerId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String engName;

    @Column(nullable = false)
    private String korName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCardType type;

    @Column(nullable = false)
    private int currentLevel = 1;

    @Column(nullable = false)
    private int requiredCount;

    @Column(nullable = false)
    private int count = 1;

    @Column(nullable = false)
    private int skillCardEnumId;

    @Column(nullable = false)
    private boolean inUse = false;

    @Column(nullable = false)
    private boolean onSale = false;

    @Column(nullable = false)
    private boolean available = true;

    @Builder
    public SkillCardEntity(Long userId, String engName, String korName, SkillCardType type,
                           int currentLevel, int requiredCount, int count, int skillCardEnumId, boolean inUse,
                           boolean onSale, boolean available, Long coworkerId
    ) {
        this.userId = userId;
        this.engName = engName;
        this.korName = korName;
        this.type = type;
        this.currentLevel = currentLevel;
        this.requiredCount = requiredCount;
        this.count = count;
        this.skillCardEnumId = skillCardEnumId;
        this.inUse = inUse;
        this.onSale = onSale;
        this.available = available;
        this.coworkerId = coworkerId;
    }

    public static SkillCardEntity of(final long userId, final ActiveSkillCard randomCard) {
        return SkillCardEntity.builder()
                .userId(userId)
                .engName(randomCard.name())
                .korName(randomCard.getKoreanName())
                .type(SkillCardType.ACTIVE)
                .currentLevel(1)
                .requiredCount(2)
                .count(1)
                .skillCardEnumId(randomCard.getId())
                .inUse(false)
                .onSale(false)
                .available(true)
                .coworkerId(0L)
                .build();
    }

    public static SkillCardEntity of(final long userId, PassiveSkillCard randomCard) {
        return SkillCardEntity.builder()
                .userId(userId)
                .engName(randomCard.name())
                .korName(randomCard.getKoreanName())
                .type(SkillCardType.PASSIVE)
                .currentLevel(1)
                .requiredCount(2)
                .count(1)
                .skillCardEnumId(randomCard.getId())
                .inUse(false)
                .onSale(false)
                .available(true)
                .coworkerId(0L)
                .build();
    }

    public void activateCoworkerId(final long coworkerId) {
        this.coworkerId = coworkerId;
        this.inUse = true;
    }

    public void deactivateCoworkerId() {
        this.coworkerId = 0L;
        this.inUse = false;
    }

    public void isMaxLevel() {
        if (this.currentLevel >= SKILL_MAX_LEVEL) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Skill card is already at max level.");
        }
    }

    public void updateFinalState(int newLevel, int finalCount) {
        this.currentLevel = newLevel;
        this.count = finalCount;

        if (newLevel < SKILL_MAX_LEVEL) {
            this.requiredCount = newLevel + 1;
        } else {
            this.requiredCount = 0;
        }
    }
}
