package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AttendanceChecker {

    public void checkDailyAttendance(final UserEntity user) {

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastAttendanceDate();

        if (lastDate != null) {
            if (lastDate.isEqual(today)) {
                return;
            }

            LocalDate yesterday = today.minusDays(1);
            if (lastDate.isBefore(yesterday)) {
                user.resetDailyAttendance();
                return;
            }

            user.dailyAttendanceCheck();
        }
        user.resetDailyAttendance();
    }
}
