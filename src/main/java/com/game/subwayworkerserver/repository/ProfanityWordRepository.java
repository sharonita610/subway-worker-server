package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.ProfanityWordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfanityWordRepository extends JpaRepository<ProfanityWordEntity, Long> {

    List<ProfanityWordEntity> findAll();
}
