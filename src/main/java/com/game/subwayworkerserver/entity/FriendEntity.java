package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.FriendAttribute;
import com.game.subwayworkerserver.domain.baseinfo.FriendType;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FriendAttribute name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int currentExp;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
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
    private int activeSkill1;

    @Column(nullable = false)
    private int activeSkill2;

    @Column(nullable = false)
    private int passiveSkill1;

    @Column(nullable = false)
    private int passiveSkill2;

    @Builder
    public FriendEntity(FriendAttribute name, int level, int currentExp, long userId, FriendType friendType, long hp, long mana, long maxMana,
                        long manaRecovery, long attack, long defense, boolean inUse, boolean onSale,
                        int activeSkill1, int activeSkill2, int passiveSkill1, int passiveSkill2
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
    }

    public static FriendEntity of(final long userId, final FriendAttribute name) {
        CharacterGenerator.CharacterStats stats = generateDefaultFriendStats();
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);

        return FriendEntity.builder()
                 .name(name)
                 .level(1)
                 .currentExp(0)
                 .userId(userId)
                 .friendType(FriendType.fromName(stats.type()))
                 .hp(stats.hp())
                 .mana(stats.mpNow())
                 .maxMana(stats.mpMax())
                 .manaRecovery(stats.manaRecovery())
                 .attack(stats.attack())
                 .defense(stats.defense())
                 .onSale(false)
                 .inUse(true)
                 .activeSkill1(randomActiveSkills.get(0).getId())
                 .activeSkill2(randomActiveSkills.get(1).getId())
                 .passiveSkill1(randomPassiveSkills.get(0).getId())
                 .passiveSkill2(randomPassiveSkills.get(1).getId())
                 .build();
    }


    public static FriendEntity of(final long userId, String friendName, int friendLevel) {
        CharacterGenerator.CharacterStats stats = getRandomFriendStat();
        List<PassiveSkillCard> randomPassiveSkills = getRandomPassiveSkills(2);
        List<ActiveSkillCard> randomActiveSkills = getRandomActiveSkills(2);


        return FriendEntity.builder()
                .name(FriendAttribute.fromKoreanName(friendName))
                .level(friendLevel)
                .currentExp(0)
                .userId(userId)
                .friendType(FriendType.fromName(stats.type()))
                .hp(stats.hp())
                .mana(stats.mpNow())
                .maxMana(stats.mpMax())
                .manaRecovery(stats.manaRecovery())
                .attack(stats.attack())
                .defense(stats.defense())
                .onSale(false)
                .inUse(false)
                .activeSkill1(randomActiveSkills.get(0).getId())
                .activeSkill2(randomActiveSkills.get(1).getId())
                .passiveSkill1(randomPassiveSkills.get(0).getId())
                .passiveSkill2(randomPassiveSkills.get(1).getId())
                .build();
    }
}
