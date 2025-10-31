package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.UserTrapMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTrapMapRepository extends JpaRepository<UserTrapMapEntity, Long> {

    // 💡 2. 지도 조회 시: 사용자 ID 기준, 만료 시간 이후가 아니며, 성공하지 않은 트랩 조회
    List<UserTrapMapEntity> findByUserIdAndExpiresAtAfterAndIsTrappedFalse(final Long userId, LocalDateTime now);

    // 💡 3. 스케줄러용: 모든 사용자 기준, 만료되지 않고 성공하지 않은 트랩 조회
    List<UserTrapMapEntity> findByExpiresAtAfterAndIsTrappedFalse(LocalDateTime now);

    // 💡 4. 성공 처리 시: 사용자 ID 기준, 성공했으며, 아직 처리되지 않은 트랩 (CapturedFriendId가 존재하는) 조회
    Optional<UserTrapMapEntity> findByUserIdAndIsTrappedTrueAndCapturedFriendIdIsNotNull(Long userId);

    // 💡 만료 시간이 지났고 (Before) 성공하지 못한 (False) 트랩을 조회 (정리용)
    List<UserTrapMapEntity> findByExpiresAtBeforeAndIsTrappedFalse(LocalDateTime now);

    List<UserTrapMapEntity> findByUserIdAndIsTrappedTrue(long userId);
}
