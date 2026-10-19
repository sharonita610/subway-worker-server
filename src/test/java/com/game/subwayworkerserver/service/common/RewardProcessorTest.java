package com.game.subwayworkerserver.service.common;

import com.game.subwayworkerserver.domain.reward.RewardType;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.domain.reward.CheckInReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.repository.MailRepository;
import com.game.subwayworkerserver.repository.RewardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardProcessorTest {

    @InjectMocks
    private RewardProcessor rewardProcessor;

    @Mock private MailRepository mailRepository;
    @Mock private RewardRepository rewardRepository;
    @Mock private UserRepository userRepository;

    private UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", "test-uuid", 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private MailEntity savedMail(long id) {
        MailEntity mail = MailEntity.builder()
                .userId(1L)
                .title("test")
                .content("test")
                .isRead(false)
                .isClaimed(false)
                .build();
        ReflectionTestUtils.setField(mail, "id", id);
        return mail;
    }

    @Test
    void _1일차_출석_보상은_코인_엔티티_1개가_저장된다() {
        // DAY_1: coin=30000, 나머지 0
        UserEntity user = createUser();
        when(mailRepository.save(any())).thenReturn(savedMail(100L));

        rewardProcessor.processAttendanceReward(user, CheckInReward.DAY_1, 1);

        ArgumentCaptor<List<RewardEntity>> captor = ArgumentCaptor.captor();
        verify(rewardRepository).saveAll(captor.capture());

        List<RewardEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getType()).isEqualTo(RewardType.COIN);
        assertThat(saved.get(0).getQuantity()).isEqualTo(30000L);
    }

    @Test
    void _2일차_출석_보상은_휴가티켓_엔티티_1개가_저장된다() {
        // DAY_2: vacationTicket=50
        UserEntity user = createUser();
        when(mailRepository.save(any())).thenReturn(savedMail(100L));

        rewardProcessor.processAttendanceReward(user, CheckInReward.DAY_2, 2);

        ArgumentCaptor<List<RewardEntity>> captor = ArgumentCaptor.captor();
        verify(rewardRepository).saveAll(captor.capture());

        List<RewardEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getType()).isEqualTo(RewardType.VACATION_TICKET);
        assertThat(saved.get(0).getQuantity()).isEqualTo(50L);
    }

    @Test
    void _7일차_출석_보상은_휴가티켓_재료친구_광고제거_엔티티_3개가_저장된다() {
        // DAY_7: vacationTicket=100, materialFriend=ENERZ_LV4, noAds=NO_ADS_3DAYS
        UserEntity user = createUser();
        when(mailRepository.save(any())).thenReturn(savedMail(100L));

        rewardProcessor.processAttendanceReward(user, CheckInReward.DAY_7, 7);

        ArgumentCaptor<List<RewardEntity>> captor = ArgumentCaptor.captor();
        verify(rewardRepository).saveAll(captor.capture());

        List<RewardEntity> saved = captor.getValue();
        assertThat(saved).hasSize(3);

        assertThat(saved).anyMatch(r -> r.getType() == RewardType.VACATION_TICKET && r.getQuantity() == 100L);
        assertThat(saved).anyMatch(r -> r.getType() == RewardType.ENERZ);
        assertThat(saved).anyMatch(r -> r.getType() == RewardType.NO_ADS);
    }
}
