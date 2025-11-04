package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "skill_card")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillCardEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private int maxLevel = 5;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false)
    private boolean inUse = false;

    @Column(nullable = false)
    private boolean onSale = false;
}
