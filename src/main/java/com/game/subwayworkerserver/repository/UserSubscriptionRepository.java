package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.UserSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {

    List<UserSubscriptionEntity> findAllByExpirationDateGreaterThanEqual(final LocalDate today);

    List<UserSubscriptionEntity> findAllByUserIdAndExpirationDateGreaterThanEqual(long userId, LocalDate expirationDate);

    Optional<UserSubscriptionEntity> findByUserIdAndTypeAndExpirationDateGreaterThanEqual(
            long userId, final SubscriptionItem subscriptionItem, LocalDate today
    );
}
