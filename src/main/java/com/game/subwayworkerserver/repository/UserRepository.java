package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByName(final String name);
}

