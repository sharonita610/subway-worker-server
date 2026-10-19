package com.game.subwayworkerserver.service.coworker;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserGachaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoworkerServiceTest {

    @InjectMocks
    private CoworkerService coworkerService;

    @Mock private CoworkerRepository coworkerRepository;
    @Mock private UserFinder userFinder;
    @Mock private SkillCardRepository skillCardRepository;
    @Mock private UserGachaRepository gachaRepository;
    @Mock private CoworkerUpgradeProcessor coworkerUpgradeProcessor;
    @Mock private CoworkerLevelProcessor coworkerLevelProcessor;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private CoworkerEntity createCoworker(long userId) {
        CoworkerEntity coworker = CoworkerEntity.builder()
                .name("testWorker")
                .level(CoworkerLevel.INTERN)
                .currentExp(0).totalExp(0)
                .userId(userId)
                .coworkerType(CoworkerType.ENERZ)
                .hp(100).initMana(50).maxMana(100).manaRecovery(10).attack(50).defense(30)
                .inUse(false).onSale(false).available(true)
                .activeSkill1(1).activeSkill2(2).passiveSkill1(1).passiveSkill2(2)
                .coworkerElement(CoworkerElement.PERFORMANCE)
                .build();
        ReflectionTestUtils.setField(coworker, "id", 10L);
        return coworker;
    }

    @Test
    void 존재하지_않는_UUID로_파트너_설정시_예외가_발생한다() {
        when(userFinder.getByUuid("unknown")).thenThrow(new BaseException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> coworkerService.configurePartners("unknown", new PartnerConfigureRequest(List.of(10L))))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 빈_목록으로_파트너_설정시_예외가_발생한다() {
        UserEntity user = createUser();
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);

        assertThatThrownBy(() -> coworkerService.configurePartners(TEST_UUID, new PartnerConfigureRequest(List.of())))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 타인_소유_동료를_파트너로_지정시_예외가_발생한다() {
        UserEntity user = createUser(); // userId = 1L
        CoworkerEntity otherUserCoworker = createCoworker(2L); // 다른 유저 소유
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndInUseTrue(1L)).thenReturn(List.of());
        when(coworkerRepository.findAllById(List.of(10L))).thenReturn(List.of(otherUserCoworker));

        assertThatThrownBy(() -> coworkerService.configurePartners(TEST_UUID, new PartnerConfigureRequest(List.of(10L))))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 유효하지_않은_재료_ID_포함시_예외가_발생한다() {
        UserEntity user = createUser();
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findById(10L)).thenReturn(Optional.of(createCoworker(1L)));
        // 재료 ID 2개 요청했는데 1개만 조회됨
        when(coworkerRepository.findAllById(List.of(20L, 30L))).thenReturn(List.of(createCoworker(1L)));

        com.game.subwayworkerserver.controller.request.UpgradeRequest request =
                new com.game.subwayworkerserver.controller.request.UpgradeRequest(10L, List.of(20L, 30L), 0);

        assertThatThrownBy(() -> coworkerService.upgradeCoworker(TEST_UUID, request))
                .isInstanceOf(BaseException.class);
    }
}
