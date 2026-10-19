package com.game.subwayworkerserver.domain.item;

import com.game.subwayworkerserver.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 리팩터링 전 트랩 성공 판정 동작을 고정하는 특성 테스트. */
class ItemNameTest {

    private static final int ROLLS = 10_000;

    private long trapSuccesses(ItemName tool) {
        return IntStream.range(0, ROLLS).filter(i -> tool.canTrapSucceed()).count();
    }

    @Test
    void 트랩_도구가_아니면_canTrapSucceed는_예외를_던진다() {
        assertThatThrownBy(ItemName.BRIEFCASE::canTrapSucceed).isInstanceOf(BaseException.class);
    }

    @Test
    void 금삽은_항상_성공한다() {
        assertThat(trapSuccesses(ItemName.GOLD_SHOVEL)).isEqualTo(ROLLS);
    }

    @Test
    void 랜덤트랩은_확률이_0이라_항상_실패한다() {
        assertThat(trapSuccesses(ItemName.RANDOM_TRAP)).isZero();
    }

    @Test
    void 나무삽은_약_50퍼센트_성공한다() {
        assertThat(trapSuccesses(ItemName.WOOD_SHOVEL)).isBetween(4_700L, 5_300L);
    }

    @Test
    void 은삽은_약_70퍼센트_성공한다() {
        assertThat(trapSuccesses(ItemName.SILVER_SHOVEL)).isBetween(6_700L, 7_300L);
    }

    @Test
    void fromConstantName은_대소문자를_구분하지_않는다() {
        assertThat(ItemName.fromConstantName("wood_shovel")).isEqualTo(ItemName.WOOD_SHOVEL);
        assertThatThrownBy(() -> ItemName.fromConstantName("no_such_item")).isInstanceOf(BaseException.class);
    }
}
