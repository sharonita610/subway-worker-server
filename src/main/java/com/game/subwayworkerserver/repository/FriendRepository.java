package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.FriendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, Long> {

    List<FriendEntity> findByUserIdAndInUseTrue(final Long userId);

    List<FriendEntity> findByUserId(final Long userId);

    List<FriendEntity> findAllByIdInAndOnSale(List<Long> ids, boolean onSale);
}
