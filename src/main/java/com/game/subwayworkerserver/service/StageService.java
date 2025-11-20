package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.DungeonRequest;
import com.game.subwayworkerserver.controller.request.StageClearRequest;
import com.game.subwayworkerserver.controller.response.DungeonResponse;
import com.game.subwayworkerserver.controller.response.StageClearResponse;
import com.game.subwayworkerserver.controller.sampleData.MonsterSampleData;
import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.domain.reward.DungeonReward;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.DungeonRecordEntity;
import com.game.subwayworkerserver.entity.StageRecordEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.DungeonRecordRepository;
import com.game.subwayworkerserver.repository.StageRecordRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final DungeonRecordRepository dungeonRecordRepository;

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
            if (requestedLevel == 1 && success) {
                StageRecordEntity stage = createStage(userId, requestedLevel);
                stage.updateStageSuccessCount();
                user.updateStageLevel(requestedLevel);

                StageRewardInfo stageRewardInfo = stageRewardProcessor.giveFirstClearReward(user, requestedLevel);
                updateReward(usingCoworkers, stageRewardInfo, user);
                return StageClearResponse.of(stageRewardInfo);

            } else if (requestedLevel == 1) {
                StageRecordEntity stage = createStage(userId, requestedLevel);
                stage.failStage(requestedLevel);
            } else {
                throw new BaseException(BAD_REQUEST, "Invalid stage level. Must start from level 1.");

            }
        }
        return StageClearResponse.empty();
    }

    private StageRecordEntity createStage(long userId, int requestedLevel) {
        StageRecordEntity newRecord = StageRecordEntity.builder()
                .userId(userId)
                .stageLevel(requestedLevel)
                .build();
        return stageRecordRepository.save(newRecord);
    }

    private void updateReward(List<CoworkerEntity> usingCoworkers, StageRewardInfo stageRewardInfo, UserEntity user) {
        usingCoworkers.forEach(coworker ->
                coworkerLevelProcessor.processCoworkerExpAndLevelUp(coworker, stageRewardInfo.coworkerExp())
        );
        userLevelProcessor.processExpGainAndLevelUp(user, stageRewardInfo.userExp());
        user.increaseCoin(stageRewardInfo.coin());
        user.processReward(stageRewardInfo);
    }

    private void processReward(final UserEntity user, final StageRewardInfo stageRewardInfo) {

    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional
    public StageClearResponse recordDungeon(final String uuid, final DungeonRequest request) {
        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();

        DungeonRecordEntity dungeonRecord = getDungeonByUserIdAnsStationNameAndUuid(
                request.stationName(), userId, request.dungeonUuId()
        );
        DungeonReward successReward = DungeonReward.SUCCESS_REWARD;
        List<CoworkerEntity> usingCoworkers = coworkerRepository.findByUserIdAndInUseTrue(userId);

        if (!dungeonRecord.canRetry()) {
            throw new BaseException(BAD_REQUEST, "입장 후 8시간 미만으로 재입장 불가합니다.");
        } else if (request.success()) {

            dungeonRecord.updateSuccess();
            updateDungeonReward(usingCoworkers, successReward, user);
            return StageClearResponse.of(successReward);

        } else {
            dungeonRecord.updateFail();
            return StageClearResponse.empty();
        }
    }

    private DungeonRecordEntity createNewDungeon(final String stationName, final long userId) {
        String dungeonUUID = UUID.randomUUID().toString().substring(0, 8);
        DungeonRecordEntity newDungeon = DungeonRecordEntity.builder()
                .userId(userId)
                .stationName(stationName)
                .isFirstClear(false)
                .isRewardPaid(false)
                .dungeonUuId(dungeonUUID)
                .build();

        return dungeonRecordRepository.save(newDungeon);
    }

    private DungeonRecordEntity getDungeonByUserIdAnsStationNameAndUuid(
            final String stationName, final long userId, final String uuid) {
        return dungeonRecordRepository.findByUserIdAndStationNameAndDungeonUuId(userId, stationName, uuid)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Dungeon uuid not found."));
    }

    private void updateDungeonReward(
            final List<CoworkerEntity> usingCoworkers, final DungeonReward successReward, final UserEntity user
    ) {
        usingCoworkers.forEach(coworker ->
                coworkerLevelProcessor.processCoworkerExpAndLevelUp(coworker, successReward.getCoworkerExp())
        );
        userLevelProcessor.processExpGainAndLevelUp(user, successReward.getUserExp());
        user.updateDungeonReward(successReward.getCoin(), successReward.getGacha(), successReward.getVacation());
    }

    @Transactional
    public DungeonResponse checkDungeon(final String uuid, final String stationName) {
        UserEntity user = getUserByUuid(uuid);
        Optional<DungeonRecordEntity> dungeon = dungeonRecordRepository.findByUserIdAndStationName(user.getId(), stationName);

        if (dungeon.isEmpty()) {
            DungeonRecordEntity newDungeon = createNewDungeon(stationName, user.getId());
            return DungeonResponse.of(true, newDungeon.getDungeonUuId());

        } else {
            DungeonRecordEntity dungeonEntity = dungeon.get();
            if (!dungeonEntity.canRetry()) {
                return DungeonResponse.of(false);
            }
            String dungeonUuId = dungeonEntity.getDungeonUuId();
            return DungeonResponse.of(true, dungeonUuId);
        }
    }
}
