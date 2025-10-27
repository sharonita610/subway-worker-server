package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.CaptureRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptureRecordRepository extends JpaRepository<CaptureRecordEntity, Long> {

    Optional<CaptureRecordEntity> findByUserId(long userId);
}
