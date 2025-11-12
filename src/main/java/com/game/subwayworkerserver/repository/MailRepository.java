package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.MailEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface MailRepository extends CrudRepository<MailEntity, Long> {

    List<MailEntity> findByUserIdAndIsReadFalseAndIsClaimedFalse(final long userId);

    List<MailEntity> findAllByUserIdAndExpiresAtAfter(long userId, LocalDate expiresAt);
}
