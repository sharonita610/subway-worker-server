package com.game.subwayworkerserver.domain.item;

import com.game.subwayworkerserver.exception.BaseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemNameTest {

    @Test
    void fromConstantName은_대소문자를_구분하지_않는다() {
        assertThat(ItemName.fromConstantName("wood_shovel")).isEqualTo(ItemName.WOOD_SHOVEL);
        assertThatThrownBy(() -> ItemName.fromConstantName("no_such_item")).isInstanceOf(BaseException.class);
    }

    @Test
    void 도구_확률은_카테고리별로_정의돼_있다() {
        assertThat(ItemName.GOLD_SHOVEL.getPercentage()).isEqualTo(100);
        assertThat(ItemName.RANDOM_TRAP.getPercentage()).isZero();
        assertThat(ItemName.BUSINESS_CARRIER.getCategory()).isEqualTo(ItemCategory.CAPTURE);
        assertThat(ItemName.WOOD_SHOVEL.getCategory()).isEqualTo(ItemCategory.TRAP);
    }
}
