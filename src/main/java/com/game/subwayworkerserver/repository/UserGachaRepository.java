package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserGachaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGachaRepository extends JpaRepository<UserGachaEntity, Long> {
}
