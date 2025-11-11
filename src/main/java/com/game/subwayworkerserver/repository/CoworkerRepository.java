package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoworkerRepository extends JpaRepository<CoworkerEntity, Long> {

    List<CoworkerEntity> findByUserIdAndInUseTrue(final Long userId);

    List<CoworkerEntity> findByUserId(final Long userId);

    List<CoworkerEntity> findAllByIdInAndOnSale(List<Long> ids, boolean onSale);

    List<CoworkerEntity> findByUserIdAndOnSaleFalseAndAvailableTrue(long userId);

    Optional<CoworkerEntity> findByUserIdAndId(final long userId, final long coworkerId);

    Optional<CoworkerEntity> findByIdAndOnSaleTrue(long coworkerId);

    Optional<CoworkerEntity> findByIdAndUserId(final long coworkerId, final long userId);

    List<CoworkerEntity> findByUserIdAndOnSaleFalseAndAvailableTrueAndInUseTrue(final long userId);
}
