package com.game.subwayworkerserver.scheduler;

import com.game.subwayworkerserver.service.gps.TrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrapCheckerScheduler {

    private final TrapService trapService;

    /**
     * 매시 30분에 실행. 만료된 트랩을 정리하고, 포획 반경 안에 들어온 동료를 포획 처리한다.
     */
    @Scheduled(cron = "0 30 * * * *")
    public void checkTrapsForCapture() {
        int successCount = trapService.checkTrapsForCapture();
        log.info("[Scheduler] 트랩 포획 확인 완료. 성공 {}건", successCount);
    }
}
