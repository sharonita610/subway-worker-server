package com.game.subwayworkerserver.service.coworker;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.domain.coworker.CoworkerUpgradeVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoworkerUpgradeProcessorTest {

    private final CoworkerUpgradeProcessor processor = new CoworkerUpgradeProcessor();

    // 재료 totalExp=100, BASE_EXTRA_EXP=50 기준
    // 동일 타입/ENERZ: Math.round((100+50)*1.2) = 180 → basePoint = (int)(180 * 0.4) = 72
    // 다른 타입:       100+50 = 150              → basePoint = (int)(150 * 0.4) = 60

    private CoworkerEntity createCoworker(CoworkerType type, int totalExp) {
        return CoworkerEntity.builder()
                .name("testWorker")
                .level(CoworkerLevel.INTERN)
                .currentExp(0)
                .totalExp(totalExp)
                .userId(1L)
                .coworkerType(type)
                .hp(100).initMana(50).maxMana(100).manaRecovery(10).attack(50).defense(30)
                .inUse(false).onSale(false).available(true)
                .activeSkill1(1).activeSkill2(2).passiveSkill1(1).passiveSkill2(2)
                .coworkerElement(CoworkerElement.PERFORMANCE)
                .build();
    }

    @Test
    void 같은_타입_재료는_1_2배_보너스가_적용된다() {
        CoworkerEntity target = createCoworker(CoworkerType.MOONGSHIL, 0);
        CoworkerEntity material = createCoworker(CoworkerType.MOONGSHIL, 100);

        CoworkerUpgradeVO result = processor.upgradeCoworker(target, List.of(material));

        assertThat(result.basePoint()).isEqualTo(72);
    }

    @Test
    void 다른_타입_재료는_기본_경험치만_적용된다() {
        CoworkerEntity target = createCoworker(CoworkerType.MOONGSHIL, 0);
        CoworkerEntity material = createCoworker(CoworkerType.MOKA, 100); // 다른 타입, ENERZ 아님

        CoworkerUpgradeVO result = processor.upgradeCoworker(target, List.of(material));

        assertThat(result.basePoint()).isEqualTo(60);
    }

    @Test
    void ENERZ_재료는_타입_무관하게_1_2배_보너스가_적용된다() {
        CoworkerEntity target = createCoworker(CoworkerType.MOONGSHIL, 0);
        CoworkerEntity material = createCoworker(CoworkerType.ENERZ, 100);

        CoworkerUpgradeVO result = processor.upgradeCoworker(target, List.of(material));

        assertThat(result.basePoint()).isEqualTo(72);
    }

    @Test
    void 재료가_여러개면_경험치가_합산된다() {
        CoworkerEntity target = createCoworker(CoworkerType.MOONGSHIL, 0);
        CoworkerEntity same = createCoworker(CoworkerType.MOONGSHIL, 100);  // 보너스: 72
        CoworkerEntity diff = createCoworker(CoworkerType.MOKA, 100);       // 기본:  60

        CoworkerUpgradeVO result = processor.upgradeCoworker(target, List.of(same, diff));

        // basePoint = (int)((180 + 150) * 0.4) = (int)(132.0) = 132
        assertThat(result.basePoint()).isEqualTo(132);
    }
}
