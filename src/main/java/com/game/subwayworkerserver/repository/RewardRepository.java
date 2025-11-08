package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.entity.RewardSource;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RewardRepository extends CrudRepository<RewardEntity, Long> {

    List<RewardEntity> findAllByMailIdInAndIsClaimedFalse(List<Long> mailIds);

    List<RewardEntity> findAllByUserIdAndIsClaimedFalseAndSource(final long userId, final RewardSource source);
}
