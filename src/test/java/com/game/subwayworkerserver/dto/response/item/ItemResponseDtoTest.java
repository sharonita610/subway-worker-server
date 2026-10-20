package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.domain.item.ItemName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseDtoTest {

    @Test
    void 휴가티켓_첫구매_상품은_구매_이력에_따라_구매_가능_여부가_달라진다() {
        List<VacationTicketStoreInfo> neverBought = VacationTicketStoreInfo.of(List.of(VacationTicket.FIRST_BUY_PACK, VacationTicket.PACK_300), false);
        List<VacationTicketStoreInfo> boughtBefore = VacationTicketStoreInfo.of(List.of(VacationTicket.FIRST_BUY_PACK), true);

        assertThat(neverBought.get(0).name()).isEqualTo("FIRST_BUY_PACK");
        assertThat(neverBought.get(0).price()).isEqualTo(4950);
        assertThat(neverBought.get(0).isFirstBuyOffer()).isTrue();
        assertThat(neverBought.get(0).isAvailable()).isTrue();
        assertThat(boughtBefore.get(0).isFirstBuyOffer()).isTrue();
        assertThat(boughtBefore.get(0).isAvailable()).isFalse();
    }

    @Test
    void 휴가티켓_일반_상품은_구매_플래그를_JSON에서_뺀다() {
        JsonNode node = json(VacationTicketStoreInfo.of(List.of(VacationTicket.PACK_300), false)).get(0);

        assertThat(node.get("name").asText()).isEqualTo("PACK_300");
        assertThat(node.get("description").asText()).isEqualTo(VacationTicket.PACK_300.getDescription());
        assertThat(node.has("isFirstBuyOffer")).isFalse();
        assertThat(node.has("isAvailable")).isFalse();
    }

    @Test
    void 포획도구_상점_정보는_랜덤_포획에만_오늘_구매_횟수와_한도를_붙인다() {
        JsonNode node = json(CaptureItemStoreInfo.of(List.of(ItemName.RANDOM_CAPTURE, ItemName.PAPER_BOX), 2));

        assertThat(node.get(0).get("engName").asText()).isEqualTo("RANDOM_CAPTURE");
        assertThat(node.get(0).get("viewedCount").asInt()).isEqualTo(2);
        assertThat(node.get(0).get("limitViewCount").asInt()).isEqualTo(3);
        assertThat(node.get(1).get("engName").asText()).isEqualTo("PAPER_BOX");
        assertThat(node.get(1).get("korName").asText()).isEqualTo("종이박스");
        assertThat(node.get(1).get("price").asInt()).isEqualTo(2500);
        assertThat(node.get(1).has("viewedCount")).isFalse();
        assertThat(node.get(1).has("limitViewCount")).isFalse();
    }

    @Test
    void 트랩도구_상점_정보는_랜덤_트랩에만_오늘_구매_횟수와_한도를_붙인다() {
        JsonNode node = json(TrapItemStoreInfo.of(List.of(ItemName.RANDOM_TRAP, ItemName.WOOD_SHOVEL), 1));

        assertThat(node.get(0).get("viewedCount").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("limitViewCount").asInt()).isEqualTo(3);
        assertThat(node.get(1).get("korName").asText()).isEqualTo("나무삽");
        assertThat(node.get(1).get("price").asInt()).isEqualTo(4000);
        assertThat(node.get(1).has("viewedCount")).isFalse();
    }

    @Test
    void 아이템_정보는_한글_이름으로_상수명을_찾는다() {
        assertThat(CaptureItemInfo.of("종이박스", 3)).isEqualTo(new CaptureItemInfo("PAPER_BOX", "종이박스", 3));
        assertThat(TrapItemInfo.of("나무삽", 2)).isEqualTo(new TrapItemInfo("WOOD_SHOVEL", "나무삽", 2));
    }

    @Test
    void 아이템_목록_응답은_고정된_등급_순서로_정렬한다() {
        List<CaptureItemInfo> captures = new ArrayList<>(List.of(
                CaptureItemInfo.of("출장용캐리어", 1), CaptureItemInfo.of("종이박스", 1), CaptureItemInfo.of("서류가방", 1)));
        List<TrapItemInfo> traps = new ArrayList<>(List.of(
                TrapItemInfo.of("금삽", 1), TrapItemInfo.of("나무삽", 1), TrapItemInfo.of("은삽", 1)));

        assertThat(CaptureItemListResponse.of(captures).captureItemList()).extracting(CaptureItemInfo::korName)
                .containsExactly("종이박스", "서류가방", "출장용캐리어");
        assertThat(TrapItemListResponse.of(traps).trapItemInfoList()).extracting(TrapItemInfo::korName)
                .containsExactly("나무삽", "은삽", "금삽");
    }

    @Test
    void 상점_응답은_네_종류_목록을_조합한다() {
        StoreItemResponse response = StoreItemResponse.of(
                List.of(ItemName.WOOD_SHOVEL, ItemName.SILVER_SHOVEL), 0,
                List.of(ItemName.PAPER_BOX), 0,
                List.of(Coin.COIN_PACK_10K, Coin.COIN_PACK_60K, Coin.COIN_PACK_140K),
                List.of(VacationTicket.values()), false);

        assertThat(response.trapItemList()).hasSize(2);
        assertThat(response.captureItemList()).hasSize(1);
        assertThat(response.coinItemInfoList()).hasSize(3);
        assertThat(response.vacationTicketList()).hasSize(4);
    }

    @Test
    void 코인_상점_정보는_티켓_가격을_price로_낸다() {
        List<CoinItemStoreInfo> infos = CoinItemStoreInfo.of(List.of(Coin.COIN_PACK_10K));

        assertThat(infos).containsExactly(new CoinItemStoreInfo("COIN_PACK_10K", Coin.COIN_PACK_10K.getDescription(), 10));
    }

    @Test
    void ItemResponse는_값을_그대로_담는다() {
        assertThat(ItemResponse.of("PAPER_BOX", "종이박스", "설명", 2))
                .isEqualTo(new ItemResponse("PAPER_BOX", "종이박스", "설명", 2));
    }
}
