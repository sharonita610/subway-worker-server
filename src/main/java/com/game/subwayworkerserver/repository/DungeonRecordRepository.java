package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.DungeonRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DungeonRecordRepository extends JpaRepository<DungeonRecordEntity, Long> {

    Optional<DungeonRecordEntity> findByUserIdAndStationName(final long userId, final String stationName);

    Optional<DungeonRecordEntity> findByUserIdAndStationNameAndDungeonUuId(long userId, String stationName, String uuid);
}
