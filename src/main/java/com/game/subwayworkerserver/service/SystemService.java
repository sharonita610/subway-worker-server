package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.LevelUpRewardResponse;
import com.game.subwayworkerserver.controller.response.UserMailBoxResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.user.MailReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MailRepository mailRepository;
    private final RewardRepository rewardRepository;
    private final CoworkerRepository coworkerRepository;

    public UserMailBoxResponse getMailBox(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<MailEntity> mailList = mailRepository.findByUserIdAndIsReadFalseAndIsClaimedFalse(user.getId());
        List<Long> mailIdList = getMailIdList(mailList);
        List<RewardEntity> allByMailIdIn = rewardRepository.findAllByMailIdInAndIsClaimedFalse(mailIdList);
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

    @Transactional
    public LevelUpRewardResponse getLevelUpReward(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<RewardEntity> levelUpReward = rewardRepository.findAllByUserIdAndIsClaimedFalseAndSource(
                user.getId(), RewardSource.LEVEL_UP
        );
        if (levelUpReward.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "현재기준 레벨업 보상이 없습니다.");
        }
        levelUpReward.forEach(reward -> {
            user.giveReward(reward.getType(), reward.getQuantity());
            reward.claimRewardDone();
        });

        user.claimRewardDone();
        return LevelUpRewardResponse.of(levelUpReward);
    }

    @Transactional
    public void claimRewardById(final String uuid, final long mailId) {
        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();

        MailEntity mail = getMail(mailId);
        List<RewardEntity> rewardList = rewardRepository.findAllByMailIdAndUserIdAndIsClaimedFalse(mail.getId(), userId);
        rewardList.forEach(reward -> {
            if(reward.getType().equals(RewardType.ENERZ)) {
                processMaterialFriend(userId);
            }
            user.giveReward(reward.getType(), reward.getQuantity());
            reward.claimRewardDone();
        });

        user.claimRewardDone();
    }

    private void processMaterialFriend(long userId) {
        CoworkerEntity coworkerEntity = CoworkerEntity.of(userId, CoworkerType.ENERZ, CoworkerLevel.getSpecialCoworkerLevel());
        coworkerRepository.save(coworkerEntity);
    }

    private MailEntity getMail(long mailId) {
        return mailRepository.findById(mailId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Mail id not found.")
        );
    }

    public void claimAllRewards(final String uuid) {

    }
}
