package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillCardMarketRepository extends JpaRepository<SkillCardMarketEntity, Long> {

    List<SkillCardMarketEntity> findAllByAvailableTrueAndSoldFalseAndSellerIdNot(final long sellerId);

    List<SkillCardMarketEntity> findAllBySellerId(final long sellerId);

    boolean existsBySkillCardIdAndSellerId(final long skillCardId, long sellerId);

    Optional<SkillCardMarketEntity> findByIdAndAvailableTrueAndSoldFalse(final long marketId);
}
