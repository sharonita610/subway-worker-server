package com.game.subwayworkerserver.service.coworker;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoworkerLevelProcessorTest {

    private final CoworkerLevelProcessor processor = new CoworkerLevelProcessor();

    private CoworkerEntity createCoworker(CoworkerLevel level) {
        return CoworkerEntity.builder()
                .name("testWorker")
                .level(level)
                .currentExp(0)
                .totalExp(0)
                .userId(1L)
                .coworkerType(CoworkerType.ENERZ)
                .hp(100).initMana(50).maxMana(100).manaRecovery(10).attack(50).defense(30)
                .inUse(true).onSale(false).available(true)
                .activeSkill1(1).activeSkill2(2).passiveSkill1(1).passiveSkill2(2)
                .coworkerElement(CoworkerElement.PERFORMANCE)
                .build();
    }

    @Test
    void 경험치_획득으로_레벨업이_발생한다() {
        CoworkerEntity coworker = createCoworker(CoworkerLevel.INTERN);
        // INTERN.requiredExpToNextLevel = 100
        processor.processCoworkerExpAndLevelUp(coworker, 100);

        assertThat(coworker.getLevel()).isEqualTo(CoworkerLevel.WORKER);
    }

    @Test
    void 경험치_부족시_레벨업이_발생하지_않는다() {
        CoworkerEntity coworker = createCoworker(CoworkerLevel.INTERN);
        processor.processCoworkerExpAndLevelUp(coworker, 99);

        assertThat(coworker.getLevel()).isEqualTo(CoworkerLevel.INTERN);
    }

    @Test
    void 연속_레벨업이_한번에_발생한다() {
        CoworkerEntity coworker = createCoworker(CoworkerLevel.INTERN);
        // INTERN->WORKER: 100, WORKER->SENIOR: 200 -> total 300
        processor.processCoworkerExpAndLevelUp(coworker, 300);

        assertThat(coworker.getLevel()).isEqualTo(CoworkerLevel.SENIOR);
    }

    @Test
    void 최대레벨에서_레벨업이_발생하지_않는다() {
        CoworkerEntity coworker = createCoworker(CoworkerLevel.CHIEF);
        processor.processCoworkerExpAndLevelUp(coworker, 999_999);

        assertThat(coworker.getLevel()).isEqualTo(CoworkerLevel.CHIEF);
    }
}
