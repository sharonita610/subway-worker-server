package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillCardMarketRepository extends JpaRepository<SkillCardMarketEntity, Long> {

    List<SkillCardMarketEntity> findAllByAvailableTrueAndSoldFalseAndSellerIdNot(final long sellerId);

    List<SkillCardMarketEntity> findAllBySellerId(final long sellerId);
}
