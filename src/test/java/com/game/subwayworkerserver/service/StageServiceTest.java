package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.DungeonRecordRepository;
import com.game.subwayworkerserver.repository.StageRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StageServiceTest {

    @InjectMocks
    private StageService stageService;

    @Mock private UserFinder userFinder;
    @Mock private CoworkerRepository coworkerRepository;
    @Mock private StageRecordRepository stageRecordRepository;
    @Mock private StageRewardProcessor stageRewardProcessor;
    @Mock private UserLevelProcessor userLevelProcessor;
    @Mock private CoworkerLevelProcessor coworkerLevelProcessor;
    @Mock private DungeonRecordRepository dungeonRecordRepository;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private CoworkerEntity createCoworker(long userId) {
        return CoworkerEntity.of(userId, com.game.subwayworkerserver.domain.baseinfo.CoworkerType.ENERZ);
    }

    @Test
    void 존재하지_않는_UUID로_스테이지_조회시_예외가_발생한다() {
        when(userFinder.getByUuid("unknown-uuid")).thenThrow(new BaseException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> stageService.checkDungeon("unknown-uuid", "강남역"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 스테이지_보상_지급시_사용중인_코워커에게_경험치가_부여된다() {
        UserEntity user = createUser();
        List<CoworkerEntity> coworkers = List.of(
                createCoworker(user.getId()),
                createCoworker(user.getId())
        );
        // StageRewardInfo(userExp, coworkerExp, coworkersTotalExp, coin, gachaTicket, vacationTicket, firstClear, extraReward)
        StageRewardInfo reward = new StageRewardInfo(100, 50, 100, 1000, 0, 0, false, null);

        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndInUseTrue(user.getId())).thenReturn(coworkers);
        when(stageRecordRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        com.game.subwayworkerserver.controller.request.StageClearRequest request =
                new com.game.subwayworkerserver.controller.request.StageClearRequest(true, 1);
        when(stageRewardProcessor.giveFirstClearReward(user, 1)).thenReturn(reward);
        when(stageRecordRepository.save(any())).thenReturn(
                com.game.subwayworkerserver.entity.StageRecordEntity.builder()
                        .userId(user.getId())
                        .stageLevel(1)
                        .build()
        );

        stageService.recordStage(TEST_UUID, request);

        // 코워커 2명 모두 경험치 부여 확인
        verify(coworkerLevelProcessor, times(2))
                .processCoworkerExpAndLevelUp(any(CoworkerEntity.class), eq(50));
        // 유저 경험치 부여 확인
        verify(userLevelProcessor).processExpGainAndLevelUp(user, 100);
    }

    @Test
    void 던전_보상도_코워커에게_경험치가_부여된다() {
        UserEntity user = createUser();
        List<CoworkerEntity> coworkers = List.of(createCoworker(user.getId()));

        com.game.subwayworkerserver.entity.DungeonRecordEntity dungeonRecord =
                com.game.subwayworkerserver.entity.DungeonRecordEntity.builder()
                        .userId(user.getId())
                        .stationName("강남역")
                        .isFirstClear(false)
                        .isRewardPaid(false)
                        .dungeonUuId("test-uuid")
                        .build();

        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndInUseTrue(user.getId())).thenReturn(coworkers);
        when(dungeonRecordRepository.findByUserIdAndStationNameAndDungeonUuId(
                user.getId(), "강남역", "test-uuid")).thenReturn(Optional.of(dungeonRecord));

        com.game.subwayworkerserver.controller.request.DungeonRequest request =
                new com.game.subwayworkerserver.controller.request.DungeonRequest("강남역", "test-uuid", true);

        stageService.recordDungeon(TEST_UUID, request);

        // updateReward와 updateDungeonReward가 동일하게 코워커 exp 부여하는지 확인
        verify(coworkerLevelProcessor, times(1))
                .processCoworkerExpAndLevelUp(
                        any(CoworkerEntity.class),
                        eq(com.game.subwayworkerserver.domain.reward.DungeonReward.SUCCESS_REWARD.getCoworkerExp())
                );
    }
}
