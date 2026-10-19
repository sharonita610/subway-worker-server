package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.domain.reward.RewardType;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.repository.RewardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserLevelProcessorTest {

    @InjectMocks
    private UserLevelProcessor userLevelProcessor;

    @Mock
    private RewardRepository rewardRepository;

    private UserEntity createUser(int level, int exp) {
        UserEntity user = new UserEntity("testUser", "test-uuid", exp, level, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 경험치_획득으로_레벨업이_발생한다() {
        UserEntity user = createUser(1, 0);
        // level 1 required exp: 1 * 200 = 200
        userLevelProcessor.processExpGainAndLevelUp(user, 200);

        assertThat(user.getLevel()).isEqualTo(2);
    }

    @Test
    void 경험치_부족시_레벨업이_발생하지_않는다() {
        UserEntity user = createUser(1, 0);
        userLevelProcessor.processExpGainAndLevelUp(user, 199);

        assertThat(user.getLevel()).isEqualTo(1);
    }

    @Test
    void 연속_레벨업이_한번에_발생한다() {
        UserEntity user = createUser(1, 0);
        // level 1→2: 200, level 2→3: 400 → total 600 for two level-ups
        userLevelProcessor.processExpGainAndLevelUp(user, 600);

        assertThat(user.getLevel()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void 최대레벨에서_레벨업이_발생하지_않는다() {
        UserEntity user = createUser(40, 0);
        userLevelProcessor.processExpGainAndLevelUp(user, 999_999);

        assertThat(user.getLevel()).isEqualTo(40);
    }

    @Test
    void 레벨업_보상에서_가챠티켓_수량이_올바르게_저장된다() {
        // LEVEL_RANGE_31_40 보상: UserLevelUpReward(coin=200000, vacationTicket=5, gachaTicket=300)
        // 버그: gachaTicket 수량에 vacationTicket 값(5)이 저장됨 → 300이어야 함
        UserEntity user = createUser(31, 0);
        // level 31 required exp: 31 * 1000 = 31000
        userLevelProcessor.processExpGainAndLevelUp(user, 31_000);

        ArgumentCaptor<List<RewardEntity>> captor = ArgumentCaptor.captor();
        verify(rewardRepository).saveAll(captor.capture());

        List<RewardEntity> savedRewards = captor.getValue();
        RewardEntity gachaTicketReward = savedRewards.stream()
                .filter(r -> r.getType() == RewardType.GACHA_TICKET)
                .findFirst()
                .orElseThrow(() -> new AssertionError("GACHA_TICKET 보상이 없습니다"));

        assertThat(gachaTicketReward.getQuantity()).isEqualTo(300);
    }

    @Test
    void 레벨업_보상에서_코인과_휴가티켓이_올바르게_저장된다() {
        // LEVEL_RANGE_31_40 보상: coin=200000, vacationTicket=5
        UserEntity user = createUser(31, 0);
        userLevelProcessor.processExpGainAndLevelUp(user, 31_000);

        ArgumentCaptor<List<RewardEntity>> captor = ArgumentCaptor.captor();
        verify(rewardRepository).saveAll(captor.capture());

        List<RewardEntity> savedRewards = captor.getValue();

        long coinRewardQuantity = savedRewards.stream()
                .filter(r -> r.getType() == RewardType.COIN)
                .mapToLong(RewardEntity::getQuantity)
                .sum();

        long vacationRewardQuantity = savedRewards.stream()
                .filter(r -> r.getType() == RewardType.VACATION_TICKET)
                .mapToLong(RewardEntity::getQuantity)
                .sum();

        assertThat(coinRewardQuantity).isEqualTo(200_000);
        assertThat(vacationRewardQuantity).isEqualTo(5);
    }
}
