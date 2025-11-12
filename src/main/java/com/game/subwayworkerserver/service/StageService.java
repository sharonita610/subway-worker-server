package com.game.subwayworkerserver.service;

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


        Optional<StageRewardInfo> rewardResult = Optional.empty();

        stageRecordRepository.findByUserId(userId)
                .ifPresentOrElse(
                        stageRecord -> {
                            int currentMaxLevel = stageRecord.getStageLevel();

                            if (requestedLevel > currentMaxLevel) {
                                if (requestedLevel == currentMaxLevel + 1) {
                                    stageRecord.updateStageLevel(requestedLevel);
                                    stageRecordRepository.save(stageRecord);

                                    StageRewardInfo result = stageRewardProcessor.giveFirstClearReward(user, requestedLevel);

                                } else {
                                    throw new BaseException(
                                            BAD_REQUEST,
                                            "Invalid stage sequence. Must clear level " + (currentMaxLevel + 1) + " next."
                                    );
                                }

                            } else if (requestedLevel < currentMaxLevel) {
                                StageRewardInfo result = stageRewardProcessor.giveRepeatClearReward(user, requestedLevel);
                            }
                        },
                        () -> {
                            if (requestedLevel == 1) {
                                StageRecordEntity newRecord = StageRecordEntity.builder()
                                        .userId(user.getId())
                                        .stageLevel(requestedLevel)
                                        .build();
                                stageRecordRepository.save(newRecord);
                                StageRewardInfo result = stageRewardProcessor.giveFirstClearReward(user, requestedLevel);

                            } else {
                                throw new BaseException(BAD_REQUEST, "Invalid stage level. Must start from level 1.");
                            }
                        });


        List<CoworkerEntity> usingCoworkers = coworkerRepository.findByUserIdAndInUseTrue(userId);

        return null;
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
