package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillCardRepository extends JpaRepository<SkillCardEntity, Long> {

    List<SkillCardEntity> findAllByIdInAndOnSaleTrue(List<Long> skillCardIds);

    List<SkillCardEntity> findAllByUserIdAndAvailableTrueAndSkillCardEnumIdInAndOnSaleFalse(Long id, List<Integer> activeSkills);

    List<SkillCardEntity> findAllByUserIdAndTypeAndAvailableTrueAndOnSaleFalse(Long id, SkillCardType skillCardType);

    Optional<SkillCardEntity> findByCoworkerIdAndTypeAndInUseTrue(Long id, SkillCardType skillCardType);

    List<SkillCardEntity> findAllByUserIdAndInUseTrueAndAvailableTrue(Long id);

    List<SkillCardEntity> findAllByUserIdAndSkillCardEnumIdAndAvailableTrueAndOnSaleFalse(final Long id, int skillCardEnumId);

    List<SkillCardEntity> findAllByIdIn(List<Long> longs);

    Optional<SkillCardEntity> findByUserIdAndIdAndInUseFalse(Long userId, long l);
}
