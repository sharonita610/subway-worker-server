package com.game.subwayworkerserver.service.gacha;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.service.gacha.GachaRandomGiftGenerator.DefaultGift;
import com.game.subwayworkerserver.service.gacha.GachaRandomGiftGenerator.ExtraGift;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 리팩터링 전 가챠 보상 생성 동작을 고정하는 특성 테스트. */
class GachaRandomGiftGeneratorTest {

    private static final int ROLLS = 10_000;

    private static int quantityOf(List<DefaultGift> gifts, String type) {
        return gifts.stream().filter(g -> g.type().equals(type)).mapToInt(DefaultGift::quantity).sum();
    }

    @Test
    void 횟수가_0이면_기본_보상이_없다() {
        assertThat(GachaRandomGiftGenerator.getDefaultReward(0)).isEmpty();
    }

    @Test
    void 기본_보상은_코인과_휴가티켓_두_종류이고_정해진_범위_안이다() {
        List<DefaultGift> gifts = GachaRandomGiftGenerator.getDefaultReward(1);

        assertThat(gifts).extracting(DefaultGift::type).containsExactlyInAnyOrder("coin", "vacationTicket");
        assertThat(quantityOf(gifts, "coin")).isBetween(10_000, 150_000);
        assertThat(quantityOf(gifts, "vacationTicket")).isBetween(20, 200);
    }

    @Test
    void 여러_번_뽑으면_종류별로_합산된다() {
        List<DefaultGift> gifts = GachaRandomGiftGenerator.getDefaultReward(3);

        assertThat(gifts).hasSize(2);
        assertThat(quantityOf(gifts, "coin")).isBetween(30_000, 450_000);
        assertThat(quantityOf(gifts, "vacationTicket")).isBetween(60, 600);
    }

    @Test
    void 횟수가_0이면_추가_보상이_없다() {
        assertThat(GachaRandomGiftGenerator.getExtraReward(0, Map.of())).isEmpty();
    }

    @Test
    void 추가_보상은_정의된_종류만_나오고_수량은_1_이상이다() {
        Set<String> allowed = new HashSet<>(List.of(
                ItemName.BRIEFCASE.name(), ItemName.BUSINESS_CARRIER.name(),
                ItemName.SILVER_SHOVEL.name(), ItemName.GOLD_SHOVEL.name(), "MATERIAL_COWORKER"));
        Arrays.stream(GachaTreasure.values()).map(Enum::name).forEach(allowed::add);

        for (int i = 0; i < 300; i++) {
            for (ExtraGift gift : GachaRandomGiftGenerator.getExtraReward(1, Map.of())) {
                assertThat(allowed).contains(gift.type());
                assertThat(gift.quantity()).isGreaterThanOrEqualTo(1);
            }
        }
    }

    @Test
    void 서류가방은_약_30퍼센트_확률로_나온다() {
        long hits = IntStream.range(0, ROLLS)
                .filter(i -> GachaRandomGiftGenerator.getExtraReward(1, Map.of()).stream()
                        .anyMatch(g -> g.type().equals(ItemName.BRIEFCASE.name())))
                .count();

        assertThat(hits).isBetween(2_700L, 3_300L);
    }

    @Test
    void 모든_보물이_보유_한도에_도달하면_예외가_발생한다() {
        Map<GachaTreasure, Integer> maxed = new EnumMap<>(GachaTreasure.class);
        for (GachaTreasure treasure : GachaTreasure.values()) {
            maxed.put(treasure, 20);
        }

        // 보물은 뽑기당 25% 확률이라 200회면 사실상 반드시 한 번은 걸린다
        assertThatThrownBy(() -> GachaRandomGiftGenerator.getExtraReward(200, maxed))
                .isInstanceOf(BaseException.class);
    }
}
