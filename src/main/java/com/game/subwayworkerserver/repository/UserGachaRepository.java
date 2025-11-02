package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGachaRepository extends JpaRepository<UserGachaEntity, Long> {

    List<UserGachaEntity> findAllByUserId(Long id);

    Optional<UserGachaEntity> findByUserIdAndGacha(long userId, GachaTreasure treasure);
}
