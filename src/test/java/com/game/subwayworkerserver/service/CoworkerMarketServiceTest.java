package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.domain.baseinfo.CurrencyType;
import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerMarketRepository;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoworkerMarketServiceTest {

    @InjectMocks
    private CoworkerMarketService coworkerMarketService;

    @Mock private UserFinder userFinder;
    @Mock private CoworkerRepository coworkerRepository;
    @Mock private CoworkerMarketRepository coworkerMarketRepository;
    @Mock private CoworkerListFilter coworkerListFilter;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser(int coin, int vacationTicket) {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, coin, vacationTicket, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private CoworkerEntity createCoworker(long userId, boolean inUse, boolean onSale) {
        CoworkerEntity coworker = CoworkerEntity.builder()
                .name("testWorker")
                .level(CoworkerLevel.INTERN)
                .currentExp(0).totalExp(0)
                .userId(userId)
                .coworkerType(CoworkerType.ENERZ)
                .hp(100).initMana(50).maxMana(100).manaRecovery(10).attack(50).defense(30)
                .inUse(inUse).onSale(onSale).available(true)
                .activeSkill1(1).activeSkill2(2).passiveSkill1(1).passiveSkill2(2)
                .coworkerElement(CoworkerElement.PERFORMANCE)
                .build();
        ReflectionTestUtils.setField(coworker, "id", 10L);
        return coworker;
    }

    private CoworkerMarketEntity createMarketEntity(long coworkerId, long sellerId, int price, int commission) {
        return new CoworkerMarketEntity(sellerId, price, commission, CurrencyType.VACATION_TICKET, coworkerId, true);
    }

    @Test
    void 존재하지_않는_UUID로_조회시_예외가_발생한다() {
        when(userFinder.getByUuid("unknown")).thenThrow(new BaseException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> coworkerMarketService.registerCoworkerOnMarket("unknown", new SellingRequest(10L, 100, 10, 50)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 코인이_부족하면_마켓_등록에_실패한다() {
        UserEntity user = createUser(0, 0);
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);

        assertThatThrownBy(() -> coworkerMarketService.registerCoworkerOnMarket(TEST_UUID, new SellingRequest(10L, 100, 10, 500)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 사용중인_동료는_등록할_수_없다() {
        UserEntity user = createUser(1000, 0);
        CoworkerEntity coworker = createCoworker(1L, true, false); // inUse=true
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(coworker));

        assertThatThrownBy(() -> coworkerMarketService.registerCoworkerOnMarket(TEST_UUID, new SellingRequest(10L, 100, 10, 100)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 이미_판매중인_동료는_등록할_수_없다() {
        UserEntity user = createUser(1000, 0);
        CoworkerEntity coworker = createCoworker(1L, false, false);
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(coworker));
        when(coworkerMarketRepository.existsByCoworkerIdAndSellerId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> coworkerMarketService.registerCoworkerOnMarket(TEST_UUID, new SellingRequest(10L, 100, 10, 100)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 동료_마켓_등록이_정상적으로_완료된다() {
        UserEntity user = createUser(1000, 0);
        CoworkerEntity coworker = createCoworker(1L, false, false);
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(coworker));
        when(coworkerMarketRepository.existsByCoworkerIdAndSellerId(10L, 1L)).thenReturn(false);

        coworkerMarketService.registerCoworkerOnMarket(TEST_UUID, new SellingRequest(10L, 100, 10, 100));

        assertThat(coworker.isOnSale()).isTrue();
        verify(coworkerMarketRepository).save(any(CoworkerMarketEntity.class));
    }

    @Test
    void 동료_구매시_휴가티켓이_차감된다() {
        UserEntity user = createUser(0, 100);
        CoworkerMarketEntity market = createMarketEntity(99L, 2L, 10, 2); // price=10, commission=2
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerMarketRepository.findByIdAndAvailableTrueAndSoldFalse(1L)).thenReturn(Optional.of(market));
        when(coworkerRepository.findByIdAndOnSaleTrue(99L)).thenReturn(Optional.empty());

        coworkerMarketService.purchaseCoworker(TEST_UUID, 1L);

        // realPrice = price(10) - commission(2) = 8
        assertThat(user.getVacationTicket()).isEqualTo(92);
    }

    @Test
    void 판매_취소시_동료_판매상태가_해제된다() {
        UserEntity user = createUser(0, 0);
        CoworkerEntity coworker = createCoworker(1L, false, true); // onSale=true
        CoworkerMarketEntity market = createMarketEntity(10L, 1L, 100, 10);
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(coworkerMarketRepository.findByIdAndAvailableTrueAndSoldFalse(1L)).thenReturn(Optional.of(market));
        when(coworkerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(coworker));

        coworkerMarketService.cancelSellingMyCoworker(TEST_UUID, 1L);

        assertThat(coworker.isOnSale()).isFalse();
        verify(coworkerMarketRepository).delete(market);
    }
}
