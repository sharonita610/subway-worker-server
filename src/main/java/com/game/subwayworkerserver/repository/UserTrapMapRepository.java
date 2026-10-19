package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserTrapMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTrapMapRepository extends JpaRepository<UserTrapMapEntity, Long> {

    List<UserTrapMapEntity> findByUserIdAndExpiresAtAfterAndIsTrappedFalse(final Long userId, LocalDateTime now);

    List<UserTrapMapEntity> findByExpiresAtAfterAndIsTrappedFalse(LocalDateTime now);

    Optional<UserTrapMapEntity> findByUserIdAndIsTrappedTrueAndCapturedCoworkerIdIsNotNull(Long userId);

    List<UserTrapMapEntity> findByExpiresAtBeforeAndIsTrappedFalse(LocalDateTime now);

    List<UserTrapMapEntity> findByUserIdAndIsTrappedTrue(long userId);
}
