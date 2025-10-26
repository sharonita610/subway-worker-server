package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.ItemName;
import com.game.subwayworkerserver.entity.UserItemMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemMapRepository extends JpaRepository<UserItemMapEntity, Long> {
    List<UserItemMapEntity> findByUserId(final Long userId);

    Optional<UserItemMapEntity> findByUserIdAndItemName(final Long userId, ItemName itemName);
}
