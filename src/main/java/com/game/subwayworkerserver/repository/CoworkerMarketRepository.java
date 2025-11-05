package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.CoworkerMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoworkerMarketRepository extends JpaRepository<CoworkerMarketEntity, Long> {

    List<CoworkerMarketEntity> findAllByAvailableTrueAndSoldFalseAndSellerIdNot(long userId);

    List<CoworkerMarketEntity> findAllBySellerId(long userId);

    Optional<CoworkerMarketEntity> findByIdAndAvailableTrueAndSoldFalse(final long marketId);

    Optional<CoworkerMarketEntity> findByIdAndSellerIdAndSoldTrueAndIsPaidFalseAndAvailableFalse(long marketId, Long id);

    boolean existsByCoworkerIdAndSellerId(final long coworkerId, final long userId);
}
