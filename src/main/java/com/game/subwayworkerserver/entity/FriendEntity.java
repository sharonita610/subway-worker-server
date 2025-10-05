package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "friend")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private long hp;
    private long mana;
    private long attack;
    private long defense;

    private boolean inUse;
    private long currentExp;
    private boolean onSale;

    private String activeSkill1;
    private String activeSkill2;
    private String passiveSkill1;
    private String passiveSkill2;
}
