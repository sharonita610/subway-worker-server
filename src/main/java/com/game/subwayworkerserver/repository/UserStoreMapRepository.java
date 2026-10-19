package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserStoreMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UserStoreMapRepository extends JpaRepository<UserStoreMapEntity, Integer> {

    boolean existsByUserIdAndItemName(Long userId, String itemName);
    long countByUserIdAndItemNameAndPurchaseDateBetween(
            Long userId,
            String itemName,
            LocalDateTime startOfToday,
            LocalDateTime endOfToday
    );

}
