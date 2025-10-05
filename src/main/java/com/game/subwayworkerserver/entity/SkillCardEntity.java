package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SkillCardEntity extends BaseTimeEntity {

    @Id
    private Long id;

    private String name;
    private boolean inUse;
    private boolean onSale;
    private SkillCardType type;
}
