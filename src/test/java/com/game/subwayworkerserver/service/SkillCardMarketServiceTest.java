package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.SkillCardMarketRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
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
class SkillCardMarketServiceTest {

    @InjectMocks
    private SkillCardMarketService skillCardMarketService;

    @Mock private UserRepository userRepository;
    @Mock private SkillCardRepository skillCardRepository;
    @Mock private SkillCardMarketRepository skillCardMarketRepository;
    @Mock private SkillCardListFilter skillCardListFilter;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser(int coin, int vacationTicket) {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, coin, vacationTicket, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private SkillCardEntity createSkillCard(long userId, boolean inUse) {
        SkillCardEntity card = SkillCardEntity.builder()
                .userId(userId)
                .engName("test_skill")
                .korName("테스트 스킬")
                .type(SkillCardType.ACTIVE)
                .currentLevel(1)
                .requiredCount(2)
                .count(1)
                .skillCardEnumId(1)
                .inUse(inUse)
                .onSale(false)
                .available(true)
                .coworkerId(0L)
                .accumulateCount(1)
                .build();
        ReflectionTestUtils.setField(card, "id", 20L);
        return card;
    }

    private SkillCardMarketEntity createMarketEntity(long skillCardId, long sellerId, int price, int commission) {
        return new SkillCardMarketEntity(sellerId, price, commission, CurrencyType.VACATION_TICKET, skillCardId, true);
    }

    @Test
    void 존재하지_않는_UUID로_조회시_예외가_발생한다() {
        when(userRepository.findByUuid("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillCardMarketService.registerCardOnMarket("unknown", new SellingRequest(20L, 100, 10, 50)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 코인이_부족하면_스킬카드_등록에_실패한다() {
        UserEntity user = createUser(0, 0);
        when(userRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> skillCardMarketService.registerCardOnMarket(TEST_UUID, new SellingRequest(20L, 100, 10, 500)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 이미_마켓에_등록된_스킬카드는_중복_등록_불가() {
        UserEntity user = createUser(1000, 0);
        SkillCardEntity card = createSkillCard(1L, false);
        when(userRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(user));
        when(skillCardRepository.findByUserIdAndIdAndInUseFalse(1L, 20L)).thenReturn(Optional.of(card));
        when(skillCardMarketRepository.existsBySkillCardIdAndSellerId(20L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> skillCardMarketService.registerCardOnMarket(TEST_UUID, new SellingRequest(20L, 100, 10, 100)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 스킬카드_마켓_등록이_정상적으로_동작한다() {
        UserEntity user = createUser(1000, 0);
        SkillCardEntity card = createSkillCard(1L, false);
        when(userRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(user));
        when(skillCardRepository.findByUserIdAndIdAndInUseFalse(1L, 20L)).thenReturn(Optional.of(card));
        when(skillCardMarketRepository.existsBySkillCardIdAndSellerId(20L, 1L)).thenReturn(false);

        skillCardMarketService.registerCardOnMarket(TEST_UUID, new SellingRequest(20L, 100, 10, 100));

        assertThat(card.isOnSale()).isTrue();
        verify(skillCardMarketRepository).save(any(SkillCardMarketEntity.class));
    }

    @Test
    void 스킬카드_구매시_휴가티켓이_차감된다() {
        UserEntity user = createUser(0, 100);
        SkillCardMarketEntity market = createMarketEntity(99L, 2L, 10, 2); // price=10, commission=2
        when(userRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(user));
        when(skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(1L)).thenReturn(Optional.of(market));
        when(skillCardRepository.findByIdAndOnSaleTrue(99L)).thenReturn(Optional.empty());

        skillCardMarketService.purchaseSkillCard(TEST_UUID, 1L);

        // realPrice = price(10) - commission(2) = 8
        assertThat(user.getVacationTicket()).isEqualTo(92);
    }

    @Test
    void 스킬카드_판매_취소가_정상적으로_동작한다() {
        UserEntity user = createUser(0, 0);
        SkillCardEntity card = createSkillCard(1L, false);
        ReflectionTestUtils.setField(card, "onSale", true);
        SkillCardMarketEntity market = createMarketEntity(20L, 1L, 100, 10);
        when(userRepository.findByUuid(TEST_UUID)).thenReturn(Optional.of(user));
        when(skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(1L)).thenReturn(Optional.of(market));
        when(skillCardRepository.findByUserIdAndIdAndInUseFalse(1L, 20L)).thenReturn(Optional.of(card));

        skillCardMarketService.cancelSellingMySkillCard(TEST_UUID, 1L);

        assertThat(card.isOnSale()).isFalse();
        verify(skillCardMarketRepository).delete(market);
    }
}
