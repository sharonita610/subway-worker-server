package com.game.subwayworkerserver.domain.stage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StageMonsterDataTest {

    @Test
    void 몇_번을_조회해도_몬스터_수가_늘어나지_않는다() {
        for (int i = 0; i < 100; i++) {
            StageMonsterData.forStage(1);
            StageMonsterData.forStage(2);
        }

        assertThat(StageMonsterData.forStage(1)).hasSize(3);
        assertThat(StageMonsterData.forStage(2)).hasSize(5);
    }

    @Test
    void 정의되지_않은_스테이지는_빈_목록이다() {
        assertThat(StageMonsterData.forStage(3)).isEmpty();
    }

    @Test
    void 돌려준_목록은_수정할_수_없다() {
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> StageMonsterData.forStage(1).clear());
    }
}
