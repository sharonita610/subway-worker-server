package com.game.subwayworkerserver.service.market;

import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class MarketValidatorTest {

    private UserEntity createUser(int coin, int vacationTicket) {
        UserEntity user = new UserEntity("testUser", "test-uuid", 0, 1, 0, coin, vacationTicket, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 코인이_충분하면_예외가_발생하지_않는다() {
        UserEntity user = createUser(500, 0);
        assertThatCode(() -> MarketValidator.checkUserCoin(100, user)).doesNotThrowAnyException();
    }

    @Test
    void 코인이_부족하면_예외가_발생한다() {
        UserEntity user = createUser(50, 0);
        assertThatThrownBy(() -> MarketValidator.checkUserCoin(100, user))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 휴가티켓이_충분하면_예외가_발생하지_않는다() {
        UserEntity user = createUser(0, 500);
        assertThatCode(() -> MarketValidator.checkUserVacationTicket(user, 100)).doesNotThrowAnyException();
    }

    @Test
    void 휴가티켓이_부족하면_예외가_발생한다() {
        UserEntity user = createUser(0, 50);
        assertThatThrownBy(() -> MarketValidator.checkUserVacationTicket(user, 100))
                .isInstanceOf(BaseException.class);
    }
}
