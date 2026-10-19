package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.controller.response.item.CaptureItemListResponse;
import com.game.subwayworkerserver.controller.response.item.TrapItemListResponse;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceItemTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserFinder userFinder;
    @Mock private UserRepository userRepository;
    @Mock private CoworkerRepository coworkerRepository;
    @Mock private UserItemMapRepository userItemMapRepository;
    @Mock private CoworkerListFilter coworkerListFilter;
    @Mock private UserGachaRepository gachaRepository;
    @Mock private SkillCardRepository skillCardRepository;
    @Mock private AttendanceChecker attendanceChecker;
    @Mock private UserSubscriptionRepository subscriptionRepository;

    private static final String TEST_UUID = "test-uuid-1234";

    private UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", TEST_UUID, 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private UserItemMapEntity captureItem(ItemName itemName) {
        return new UserItemMapEntity(1L, itemName, 5);
    }

    private UserItemMapEntity trapItem(ItemName itemName) {
        return new UserItemMapEntity(1L, itemName, 3);
    }

    @Test
    void 포획_아이템_목록은_CAPTURE_카테고리만_반환한다() {
        UserEntity user = createUser();
        List<UserItemMapEntity> allItems = List.of(
                captureItem(ItemName.PAPER_BOX),
                captureItem(ItemName.BRIEFCASE),
                trapItem(ItemName.WOOD_SHOVEL),
                trapItem(ItemName.SILVER_SHOVEL)
        );

        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(userItemMapRepository.findByUserId(user.getId())).thenReturn(allItems);

        CaptureItemListResponse response = userService.getCaptureItemList(TEST_UUID);

        assertThat(response.captureItemList()).hasSize(2);
        assertThat(response.captureItemList())
                .allMatch(item -> List.of("종이박스", "서류가방").contains(item.korName()));
    }

    @Test
    void 트랩_아이템_목록은_TRAP_카테고리만_반환한다() {
        UserEntity user = createUser();
        List<UserItemMapEntity> allItems = List.of(
                captureItem(ItemName.PAPER_BOX),
                captureItem(ItemName.BRIEFCASE),
                trapItem(ItemName.WOOD_SHOVEL),
                trapItem(ItemName.SILVER_SHOVEL)
        );

        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(userItemMapRepository.findByUserId(user.getId())).thenReturn(allItems);

        TrapItemListResponse response = userService.getTrapItemList(TEST_UUID);

        assertThat(response.trapItemInfoList()).hasSize(2);
        assertThat(response.trapItemInfoList())
                .allMatch(item -> List.of("나무삽", "은삽").contains(item.korName()));
    }

    @Test
    void 아이템이_없으면_빈_목록을_반환한다() {
        UserEntity user = createUser();

        when(userFinder.getByUuid(TEST_UUID)).thenReturn(user);
        when(userItemMapRepository.findByUserId(user.getId())).thenReturn(List.of());

        CaptureItemListResponse captureResponse = userService.getCaptureItemList(TEST_UUID);
        TrapItemListResponse trapResponse = userService.getTrapItemList(TEST_UUID);

        assertThat(captureResponse.captureItemList()).isEmpty();
        assertThat(trapResponse.trapItemInfoList()).isEmpty();
    }

    @Test
    void 존재하지_않는_UUID로_조회시_예외가_발생한다() {
        when(userFinder.getByUuid("unknown-uuid")).thenThrow(new BaseException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> userService.getCaptureItemList("unknown-uuid"))
                .isInstanceOf(BaseException.class);
    }
}
