package com.game.subwayworkerserver.service.gacha;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.controller.request.gacha.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserGachaRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GachaServiceTest {

    @InjectMocks
    private GachaService gachaService;

    @Mock private UserFinder userFinder;
    @Mock private UserGachaRepository gachaRepository;
    @Mock private CoworkerRepository coworkerRepository;
    @Mock private UserItemMapRepository userItemMapRepository;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser(int gachaTicket, int vacationTicket) {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, 0, vacationTicket, gachaTicket, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 존재하지_않는_UUID로_조회시_예외가_발생한다() {
        when(userFinder.getByUuid("unknown")).thenThrow(new BaseException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> gachaService.getGachaList("unknown"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 가챠티켓이_부족하면_가챠_오픈에_실패한다() {
        UserEntity user = createUser(0, 0); // gachaTicket = 0
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);

        // GACHA_PULL: currencyType=GACHA_TICKET, price=1
        OpenLuckyGachaRequest request = new OpenLuckyGachaRequest("gachaTicket", 1);

        assertThatThrownBy(() -> gachaService.openLuckyGacha(TEST_UUID, request))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 휴가티켓이_부족하면_가챠_오픈에_실패한다() {
        UserEntity user = createUser(0, 0); // vacationTicket = 0
        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);

        // SINGLE_PULL: currencyType=VACATION_TICKET, price=60
        OpenLuckyGachaRequest request = new OpenLuckyGachaRequest("vacationTicket", 60);

        assertThatThrownBy(() -> gachaService.openLuckyGacha(TEST_UUID, request))
                .isInstanceOf(BaseException.class);
    }
}
