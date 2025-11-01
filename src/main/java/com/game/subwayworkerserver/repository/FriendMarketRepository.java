package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.FriendMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendMarketRepository extends JpaRepository<FriendMarketEntity, Long> {

    List<FriendMarketEntity> findAllByAvailableTrueAndSoldFalse();
}
