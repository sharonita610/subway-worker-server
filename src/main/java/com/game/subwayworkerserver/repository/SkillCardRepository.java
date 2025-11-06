package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillCardRepository extends JpaRepository<SkillCardEntity, Long> {

    List<SkillCardEntity> findAllByIdInAndOnSaleTrue(List<Long> skillCardIds);

    List<SkillCardEntity> findAllByCoworkerIdAndAvailableTrueAndSkillCardEnumIdIn(Long id, List<Integer> activeSkills);
}
