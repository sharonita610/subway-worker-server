package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillCardRepository extends JpaRepository<SkillCardEntity, Long> {

    List<SkillCardEntity> findAllByIdInAndOnSaleTrue(List<Long> skillCardIds);

    List<SkillCardEntity> findAllUserIdAndAvailableTrueAndSkillCardEnumIdIn(Long id, List<Integer> activeSkills);

    List<SkillCardEntity> findAllByUserIdAndTypeAndAvailableTrue(Long id, SkillCardType skillCardType);
}
