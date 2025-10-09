package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserItemMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemMapRepository extends JpaRepository<UserItemMapEntity, Long> {
    List<UserItemMapEntity> findByUserId(Long id);
}
