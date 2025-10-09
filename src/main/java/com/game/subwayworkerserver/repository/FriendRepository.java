package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.FriendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository  extends JpaRepository<FriendEntity, Long> {
}
