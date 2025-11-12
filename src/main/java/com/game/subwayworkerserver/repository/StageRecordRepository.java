package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.StageRecordEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StageRecordRepository extends CrudRepository<StageRecordEntity, Long> {

    Optional<StageRecordEntity> findByUserId(final long userId);
}
