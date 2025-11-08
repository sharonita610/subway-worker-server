package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.UserMailBoxResponse;
import com.game.subwayworkerserver.domain.user.MailReward;
import com.game.subwayworkerserver.entity.MailEntity;
import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;

    public UserMailBoxResponse getMailBox(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<MailEntity> mailList = mailRepository.findByUserIdAndIsReadFalseAndIsClaimedFalse(user.getId());
        List<Long> mailIdList = getMailIdList(mailList);
        List<RewardEntity> allByMailIdIn = rewardRepository.findAllByMailIdIn(mailIdList);
        List<MailReward> domainList = MailReward.toDomain(mailList, allByMailIdIn);
        return UserMailBoxResponse.of(domainList);
    }

    private static List<Long> getMailIdList(List<MailEntity> mailList) {
        return mailList.stream()
                .map(MailEntity::getId)
                .toList();
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
