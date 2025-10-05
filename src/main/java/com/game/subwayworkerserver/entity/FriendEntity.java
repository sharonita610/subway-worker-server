package com.game.subwayworkerserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
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
