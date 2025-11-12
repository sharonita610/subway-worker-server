package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.DungeonRequest;
import com.game.subwayworkerserver.controller.request.StageClearRequest;
import com.game.subwayworkerserver.controller.response.StageClearResponse;
import com.game.subwayworkerserver.controller.sampleData.MonsterSampleData;
import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.StageRecordEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.StageRecordRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

@Component
@RequiredArgsConstructor
public class StageService {

    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final StageRecordRepository stageRecordRepository;
    private final StageRewardProcessor stageRewardProcessor;
    private final UserLevelProcessor userLevelProcessor;
    private final CoworkerLevelProcessor coworkerLevelProcessor;

    public List<MonsterSampleData.MonsterType> getStageMonsters(int num) {
        MonsterSampleData.initializeData();
        List<MonsterSampleData.MonsterType> selectedMonsters = null;

        if (num == 1) {
            selectedMonsters = MonsterSampleData.sampleMonster1;
        } else if (num == 2) {
            selectedMonsters = MonsterSampleData.sampleMonster2;
        }

        return selectedMonsters;
    }

    @Transactional
    public StageClearResponse recordStage(final String uuid, final StageClearRequest request) {
        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();
        int requestedLevel = request.level();
        boolean success = request.success();

        List<CoworkerEntity> usingCoworkers = coworkerRepository.findByUserIdAndInUseTrue(userId);
        Optional<StageRecordEntity> stageRecordEntity = stageRecordRepository.findByUserId(userId);

        if (stageRecordEntity.isPresent() && success) {
            StageRecordEntity stageRecord = stageRecordEntity.get();
            int currentMaxLevel = stageRecord.getStageLevel();

            if (requestedLevel > currentMaxLevel) {
                if (requestedLevel == currentMaxLevel + 1) {
                    stageRecord.updateStageLevel(requestedLevel);
                    user.updateStageLevel(requestedLevel);
                    StageRewardInfo stageRewardInfo = stageRewardProcessor.giveFirstClearReward(user, requestedLevel);
                    updateReward(usingCoworkers, stageRewardInfo, user);
                    return StageClearResponse.of(stageRewardInfo);
                } else {
                    throw new BaseException(
                            BAD_REQUEST,
                            "Invalid stage sequence. Must clear level " + (currentMaxLevel + 1) + " next."
                    );
                }

            } else {
                StageRewardInfo stageRewardInfo = stageRewardProcessor.giveRepeatClearReward(user, requestedLevel);
                updateReward(usingCoworkers, stageRewardInfo, user);
                return StageClearResponse.of(stageRewardInfo);

            }
        } else if (stageRecordEntity.isPresent()) {
            StageRecordEntity stageRecord = stageRecordEntity.get();
            stageRecord.failStage(requestedLevel);
        } else {
            if (requestedLevel == 1) {
                StageRecordEntity newRecord = StageRecordEntity.builder()
                        .userId(userId)
                        .stageLevel(requestedLevel)
                        .build();
                stageRecordRepository.save(newRecord);
                user.updateStageLevel(requestedLevel);

                StageRewardInfo stageRewardInfo = stageRewardProcessor.giveFirstClearReward(user, requestedLevel);
                updateReward(usingCoworkers, stageRewardInfo, user);
                return StageClearResponse.of(stageRewardInfo);

            } else {
                throw new BaseException(BAD_REQUEST, "Invalid stage level. Must start from level 1.");

            }
        }
        return StageClearResponse.empty();
    }

    private void updateReward(List<CoworkerEntity> usingCoworkers, StageRewardInfo stageRewardInfo, UserEntity user) {
        usingCoworkers.forEach(coworker ->
                coworkerLevelProcessor.processCoworkerExpAndLevelUp(coworker, stageRewardInfo.coworkerExp())
        );
        userLevelProcessor.processExpGainAndLevelUp(user, stageRewardInfo.userExp());
        user.increaseCoin(stageRewardInfo.coin());
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    public StageClearResponse recordDungeon(String uuid, DungeonRequest request) {
        return null;
    }
}
