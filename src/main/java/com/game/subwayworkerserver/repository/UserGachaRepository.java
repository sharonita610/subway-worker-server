package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserGachaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGachaRepository extends JpaRepository<UserGachaEntity, Long> {

    List<UserGachaEntity> findAllByUserId(Long id);
}
