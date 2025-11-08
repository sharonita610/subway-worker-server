package com.game.subwayworkerserver.repository;

import com.game.subwayworkerserver.entity.MailEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MailRepository extends CrudRepository<MailEntity, Long> {

    List<MailEntity> findByUserIdAndIsReadFalseAndIsClaimedFalse(Long id);
}
