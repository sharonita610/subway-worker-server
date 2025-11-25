package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByName(final String name);

    Optional<UserEntity> findByUuid(final String uuid);

    @Query("SELECT u.uuid FROM UserEntity u") // JPQL 사용: UserEntity 테이블에서 UUID 컬럼만 선택
    List<String> getAllUserUuid();

    @Query("SELECT u.uuid FROM UserEntity u WHERE u.realName = :realName AND u.email = :email")
    Optional<String> findUuidByRealNameAndEmail(@Param("realName") final String realName, @Param("email") final String email);

    @Query("SELECT u.uuid FROM UserEntity u WHERE u.tossHash = :tossHash ")
    Optional<String> findUuidByTossHash(@Param("tossHash") String tossHash);
}

