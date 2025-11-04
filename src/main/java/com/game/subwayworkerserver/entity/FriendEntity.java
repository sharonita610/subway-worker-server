package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.*;
import com.game.subwayworkerserver.util.CharacterGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static com.game.subwayworkerserver.util.CharacterGenerator.generateDefaultFriendStats;
import static com.game.subwayworkerserver.util.CharacterGenerator.getRandomFriendStat;
import static com.game.subwayworkerserver.util.SkillRandomizer.getRandomActiveSkills;
import static com.game.subwayworkerserver.util.SkillRandomizer.getRandomPassiveSkills;

@Getter
@Entity
@Table(name = "friend")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendEntity extends BaseTimeEntity {

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
    private FriendLevel level;

    @Column(nullable = false)
    private int currentExp;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FriendType friendType;

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
    private FriendAttribute friendAttribute;

    @Column(nullable = false)
    private boolean isMaterial = false;

    @Builder
    public FriendEntity(String name, FriendLevel level, int currentExp, long userId, FriendType friendType, long hp,
                        long mana, long maxMana, long manaRecovery, long attack, long defense, boolean inUse,
                        boolean onSale, int activeSkill1, int activeSkill2, int passiveSkill1, int passiveSkill2,
                        FriendAttribute friendAttribute, boolean available,  boolean isMaterial
    ) {
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.userId = userId;
        this.friendType = friendType;
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
        this.friendAttribute = friendAttribute;
        this.available = available;
        this.isMaterial = isMaterial;
    }

    public static FriendEntity of(final long userId, final FriendType friendType) {
        CharacterGenerator.CharacterStats stats = generateDefaultFriendStats(friendType);
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);

        return FriendEntity.builder()
                .name(friendType.getKoreanName())
                .level(FriendLevel.of(ONE))
                .currentExp(DEFAULT)
                .userId(userId)
                .friendType(friendType)
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
                .friendAttribute(stats.attribute())
                .build();
    }


    public static FriendEntity of(final long userId, String friendName, int friendLevel) {
        FriendType friendType = FriendType.fromName(friendName);
        CharacterGenerator.CharacterStats stats = getRandomFriendStat(friendType);
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);

        return FriendEntity.builder()
                .name(friendName)
                .level(FriendLevel.of(friendLevel))
                .currentExp(DEFAULT)
                .userId(userId)
                .friendType(friendType)
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
                .friendAttribute(stats.attribute())
                .build();
    }

    public static FriendEntity copyEntityForRegistration(final FriendEntity friendEntity) {
        return FriendEntity.builder()
                .name(friendEntity.getName())
                .level(friendEntity.getLevel())
                .currentExp(friendEntity.getCurrentExp())
                .userId(friendEntity.getUserId())
                .friendType(friendEntity.getFriendType())
                .hp(friendEntity.getHp())
                .mana(friendEntity.getMana())
                .maxMana(friendEntity.getMaxMana())
                .manaRecovery(friendEntity.getManaRecovery())
                .attack(friendEntity.getAttack())
                .defense(friendEntity.getDefense())
                .onSale(true)
                .inUse(false)
                .available(false)
                .activeSkill1(friendEntity.getActiveSkill1())
                .activeSkill2(friendEntity.getActiveSkill2())
                .passiveSkill1(friendEntity.getPassiveSkill1())
                .passiveSkill2(friendEntity.getPassiveSkill2())
                .friendAttribute(friendEntity.getFriendAttribute())
                .build();
    }

    public static FriendEntity of(final long userId, final FriendType friendType, final FriendLevel level) {
        return FriendEntity.builder()
                .name(friendType.getKoreanName())
                .level(level)
                .currentExp(DEFAULT)
                .userId(userId)
                .friendType(friendType)
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
                .friendAttribute(FriendAttribute.getRandomAttribute())
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

    public void friendOnSale() {
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
