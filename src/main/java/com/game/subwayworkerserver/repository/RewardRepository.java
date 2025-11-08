package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.RewardEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RewardRepository extends CrudRepository<RewardEntity, Long> {

    List<RewardEntity> findAllByMailIdIn(List<Long> mailIds);
}
