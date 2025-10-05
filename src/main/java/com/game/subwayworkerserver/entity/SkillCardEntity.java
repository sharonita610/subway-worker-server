package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    private String name;
    private boolean inUse;
    private boolean onSale;
    private SkillCardType type;
}
