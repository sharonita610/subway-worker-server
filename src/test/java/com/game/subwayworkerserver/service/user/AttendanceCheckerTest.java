package com.game.subwayworkerserver.service.user;

import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceCheckerTest {

    private final AttendanceChecker attendanceChecker = new AttendanceChecker();

    private UserEntity createUser() {
        UserEntity user = new UserEntity("testUser", "test-uuid", 0, 1, 0, 0, 0, 0, 0, Gender.MALE);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void 오늘_이미_출석하면_아무것도_변경되지_않는다() {
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "lastAttendanceDate", LocalDate.now());
        ReflectionTestUtils.setField(user, "consecutiveAttendanceDays", 3);

        attendanceChecker.checkDailyAttendance(user);

        assertThat(user.getConsecutiveAttendanceDays()).isEqualTo(3);
    }

    @Test
    void 어제_출석했으면_연속_출석_횟수가_증가한다() {
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "lastAttendanceDate", LocalDate.now().minusDays(1));
        ReflectionTestUtils.setField(user, "consecutiveAttendanceDays", 1);

        attendanceChecker.checkDailyAttendance(user);

        assertThat(user.getConsecutiveAttendanceDays()).isEqualTo(2);
    }

    @Test
    void 출석_기록이_없으면_1일부터_시작한다() {
        UserEntity user = createUser(); // lastAttendanceDate = null

        attendanceChecker.checkDailyAttendance(user);

        assertThat(user.getConsecutiveAttendanceDays()).isEqualTo(1);
    }

    @Test
    void 이틀_이상_지나면_스트릭이_초기화된다() {
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "lastAttendanceDate", LocalDate.now().minusDays(2));
        ReflectionTestUtils.setField(user, "consecutiveAttendanceDays", 5);

        attendanceChecker.checkDailyAttendance(user);

        assertThat(user.getConsecutiveAttendanceDays()).isEqualTo(1);
    }
}
