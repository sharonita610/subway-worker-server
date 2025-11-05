package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.*;
import com.game.subwayworkerserver.util.CharacterGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static com.game.subwayworkerserver.util.CharacterGenerator.generateDefaultCoworkerStats;
import static com.game.subwayworkerserver.util.CharacterGenerator.getRandomCoworkerStat;
import static com.game.subwayworkerserver.util.SkillRandomizer.getRandomActiveSkills;
import static com.game.subwayworkerserver.util.SkillRandomizer.getRandomPassiveSkills;

@Getter
@Entity
@Table(name = "coworker")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoworkerEntity extends BaseTimeEntity {

    private static final int DEFAULT = 0;
    private static final int ZERO = 0;
    private static final int ONE = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CoworkerLevel level;

    @Column(nullable = false)
    private int currentExp;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CoworkerType coworkerType;

    @Column(nullable = false)
    private long hp;

    @Column(nullable = false)
    private long mana;

    @Column(nullable = false)
    private long maxMana;

    @Column(nullable = false)
    private long manaRecovery;

    @Column(nullable = false)
    private long attack;

    @Column(nullable = false)
    private long defense;

    @Column(nullable = false)
    private boolean inUse;

    @Column(nullable = false)
    private boolean onSale;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private int activeSkill1;

    @Column(nullable = false)
    private int activeSkill2;

    @Column(nullable = false)
    private int passiveSkill1;

    @Column(nullable = false)
    private int passiveSkill2;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CoworkerElement coworkerElement;

    @Column(nullable = false)
    private boolean isMaterial = false;

    @Builder
    public CoworkerEntity(String name, CoworkerLevel level, int currentExp, long userId, CoworkerType coworkerType, long hp,
                          long mana, long maxMana, long manaRecovery, long attack, long defense, boolean inUse,
                          boolean onSale, int activeSkill1, int activeSkill2, int passiveSkill1, int passiveSkill2,
                          CoworkerElement coworkerElement, boolean available, boolean isMaterial
    ) {
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.userId = userId;
        this.coworkerType = coworkerType;
        this.hp = hp;
        this.mana = mana;
        this.maxMana = maxMana;
        this.manaRecovery = manaRecovery;
        this.attack = attack;
        this.defense = defense;
        this.inUse = inUse;
        this.onSale = onSale;
        this.activeSkill1 = activeSkill1;
        this.activeSkill2 = activeSkill2;
        this.passiveSkill1 = passiveSkill1;
        this.passiveSkill2 = passiveSkill2;
        this.coworkerElement = coworkerElement;
        this.available = available;
        this.isMaterial = isMaterial;
    }

    public static CoworkerEntity of(final long userId, final CoworkerType coworkerType) {
        CharacterGenerator.CharacterStats stats = generateDefaultCoworkerStats(coworkerType);
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);

        return CoworkerEntity.builder()
                .name(coworkerType.getKoreanName())
                .level(CoworkerLevel.of(ONE))
                .currentExp(DEFAULT)
                .userId(userId)
                .coworkerType(coworkerType)
                .hp(stats.hp())
                .mana(stats.mpNow())
                .maxMana(stats.mpMax())
                .manaRecovery(stats.manaRecovery())
                .attack(stats.attack())
                .defense(stats.defense())
                .onSale(false)
                .inUse(true)
                .available(true)
                .activeSkill1(randomActiveSkills.get(ZERO).getId())
                .activeSkill2(randomActiveSkills.get(ONE).getId())
                .passiveSkill1(randomPassiveSkills.get(ZERO).getId())
                .passiveSkill2(randomPassiveSkills.get(ONE).getId())
                .coworkerElement(stats.attribute())
                .build();
    }


    public static CoworkerEntity of(final long userId, String coworkerName, int coworkerLevel) {
        CoworkerType coworkerType = CoworkerType.fromName(coworkerName);
        CharacterGenerator.CharacterStats stats = getRandomCoworkerStat(coworkerType);
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);

        return CoworkerEntity.builder()
                .name(coworkerName)
                .level(CoworkerLevel.of(coworkerLevel))
                .currentExp(DEFAULT)
                .userId(userId)
                .coworkerType(coworkerType)
                .hp(stats.hp())
                .mana(stats.mpNow())
                .maxMana(stats.mpMax())
                .manaRecovery(stats.manaRecovery())
                .attack(stats.attack())
                .defense(stats.defense())
                .onSale(false)
                .inUse(false)
                .available(true)
                .activeSkill1(randomActiveSkills.get(ZERO).getId())
                .activeSkill2(randomActiveSkills.get(ONE).getId())
                .passiveSkill1(randomPassiveSkills.get(ZERO).getId())
                .passiveSkill2(randomPassiveSkills.get(ONE).getId())
                .coworkerElement(stats.attribute())
                .build();
    }

    public static CoworkerEntity copyEntityForRegistration(final CoworkerEntity coworkerEntity) {
        return CoworkerEntity.builder()
                .name(coworkerEntity.getName())
                .level(coworkerEntity.getLevel())
                .currentExp(coworkerEntity.getCurrentExp())
                .userId(coworkerEntity.getUserId())
                .coworkerType(coworkerEntity.getCoworkerType())
                .hp(coworkerEntity.getHp())
                .mana(coworkerEntity.getMana())
                .maxMana(coworkerEntity.getMaxMana())
                .manaRecovery(coworkerEntity.getManaRecovery())
                .attack(coworkerEntity.getAttack())
                .defense(coworkerEntity.getDefense())
                .onSale(true)
                .inUse(false)
                .available(false)
                .activeSkill1(coworkerEntity.getActiveSkill1())
                .activeSkill2(coworkerEntity.getActiveSkill2())
                .passiveSkill1(coworkerEntity.getPassiveSkill1())
                .passiveSkill2(coworkerEntity.getPassiveSkill2())
                .coworkerElement(coworkerEntity.getCoworkerElement())
                .build();
    }

    public static CoworkerEntity of(final long userId, final CoworkerType coworkerType, final CoworkerLevel level) {
        return CoworkerEntity.builder()
                .name(coworkerType.getKoreanName())
                .level(level)
                .currentExp(DEFAULT)
                .userId(userId)
                .coworkerType(coworkerType)
                .hp(DEFAULT)
                .mana(DEFAULT)
                .maxMana(DEFAULT)
                .manaRecovery(DEFAULT)
                .attack(DEFAULT)
                .defense(DEFAULT)
                .onSale(false)
                .inUse(false)
                .available(false)
                .activeSkill1(DEFAULT)
                .activeSkill2(DEFAULT)
                .passiveSkill1(DEFAULT)
                .passiveSkill2(DEFAULT)
                .isMaterial(true)
                .coworkerElement(CoworkerElement.getRandomAttribute())
                .build();
    }

    public List<ActiveSkillCard> getActiveSkills() {
        return List.of(
                ActiveSkillCard.from(this.getActiveSkill1()),
                ActiveSkillCard.from(this.getActiveSkill2())
        );
    }

    public List<PassiveSkillCard> getPassiveSkills() {
        return List.of(
                PassiveSkillCard.from(this.getPassiveSkill1()),
                PassiveSkillCard.from(this.getPassiveSkill2())
        );
    }

    public void coworkerOnSale() {
        this.onSale = true;
    }

    public void cancelSelling() {
        this.onSale = false;
    }

    public void registerNewUser(final long buyerId) {
        this.userId = buyerId;
        this.onSale = false;
        this.available = true;
    }

    public void paidDone() {
        this.onSale = false;
    }
}
