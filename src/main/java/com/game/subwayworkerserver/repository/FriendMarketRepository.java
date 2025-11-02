package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.FriendMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendMarketRepository extends JpaRepository<FriendMarketEntity, Long> {

    List<FriendMarketEntity> findAllByAvailableTrueAndSoldFalseAndSellerIdNot(long userId);

    List<FriendMarketEntity> findAllBySellerId(long userId);

    Optional<FriendMarketEntity> findByIdAndAvailableTrueAndSoldFalse(final long marketId);

    Optional<FriendMarketEntity> findByIdAndSellerIdAndSoldTrueAndIsPaidFalseAndAvailableFalse(long marketId, Long id);

    boolean existsByFriendIdAndSellerId(final long friendId, final long userId);
}
