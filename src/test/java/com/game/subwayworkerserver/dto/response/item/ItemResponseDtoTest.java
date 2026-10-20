package com.game.subwayworkerserver.dto.response.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.domain.item.ItemName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseDtoTest {

    @Test
    void 휴가티켓_첫구매_상품은_구매_이력에_따라_구매_가능_여부가_달라진다() {
        List<PackStoreInfo> neverBought = PackStoreInfo.ofVacationTickets(List.of(VacationTicket.FIRST_BUY_PACK, VacationTicket.PACK_300), false);
        List<PackStoreInfo> boughtBefore = PackStoreInfo.ofVacationTickets(List.of(VacationTicket.FIRST_BUY_PACK), true);

        assertThat(neverBought.get(0).name()).isEqualTo("FIRST_BUY_PACK");
        assertThat(neverBought.get(0).price()).isEqualTo(4950);
        assertThat(neverBought.get(0).isFirstBuyOffer()).isTrue();
        assertThat(neverBought.get(0).isAvailable()).isTrue();
        assertThat(boughtBefore.get(0).isFirstBuyOffer()).isTrue();
        assertThat(boughtBefore.get(0).isAvailable()).isFalse();
    }

    @Test
    void 일반_팩과_코인_팩은_첫구매_플래그를_JSON에서_뺀다() {
        JsonNode ticket = json(PackStoreInfo.ofVacationTickets(List.of(VacationTicket.PACK_300), false)).get(0);
        JsonNode coin = json(PackStoreInfo.ofCoins(List.of(Coin.COIN_PACK_10K))).get(0);

        assertThat(ticket.get("name").asText()).isEqualTo("PACK_300");
        assertThat(ticket.get("description").asText()).isEqualTo(VacationTicket.PACK_300.getDescription());
        assertThat(ticket.has("isFirstBuyOffer")).isFalse();
        assertThat(ticket.has("isAvailable")).isFalse();
        assertThat(coin.get("name").asText()).isEqualTo("COIN_PACK_10K");
        assertThat(coin.get("price").asInt()).isEqualTo(10);
        assertThat(coin.has("isFirstBuyOffer")).isFalse();
    }

    @Test
    void 상점_도구_정보는_랜덤_도구에만_오늘_구매_횟수와_한도를_붙인다() {
        JsonNode captures = json(StoreItemInfo.of(List.of(ItemName.RANDOM_CAPTURE, ItemName.PAPER_BOX), 2));
        JsonNode traps = json(StoreItemInfo.of(List.of(ItemName.RANDOM_TRAP, ItemName.WOOD_SHOVEL), 1));

        assertThat(captures.get(0).get("engName").asText()).isEqualTo("RANDOM_CAPTURE");
        assertThat(captures.get(0).get("viewedCount").asInt()).isEqualTo(2);
        assertThat(captures.get(0).get("limitViewCount").asInt()).isEqualTo(3);
        assertThat(captures.get(1).get("korName").asText()).isEqualTo("종이박스");
        assertThat(captures.get(1).get("price").asInt()).isEqualTo(2500);
        assertThat(captures.get(1).has("viewedCount")).isFalse();
        assertThat(traps.get(0).get("viewedCount").asInt()).isEqualTo(1);
        assertThat(traps.get(1).get("korName").asText()).isEqualTo("나무삽");
        assertThat(traps.get(1).has("limitViewCount")).isFalse();
    }

    @Test
    void 아이템_정보는_설명을_요청할_때만_담는다() {
        JsonNode plain = json(ItemInfo.of(ItemName.PAPER_BOX, 3));
        ItemInfo detailed = ItemInfo.withDescription(ItemName.WOOD_SHOVEL, 2);

        assertThat(plain.get("engName").asText()).isEqualTo("PAPER_BOX");
        assertThat(plain.get("korName").asText()).isEqualTo("종이박스");
        assertThat(plain.has("description")).isFalse();
        assertThat(detailed).isEqualTo(new ItemInfo("WOOD_SHOVEL", "나무삽", ItemName.WOOD_SHOVEL.getDescription(), 2));
    }

    @Test
    void 아이템_목록_응답은_하급에서_상급_순서로_정렬한다() {
        List<ItemInfo> captures = List.of(ItemInfo.of(ItemName.BUSINESS_CARRIER, 1), ItemInfo.of(ItemName.PAPER_BOX, 1), ItemInfo.of(ItemName.BRIEFCASE, 1));
        List<ItemInfo> traps = List.of(ItemInfo.of(ItemName.GOLD_SHOVEL, 1), ItemInfo.of(ItemName.WOOD_SHOVEL, 1), ItemInfo.of(ItemName.SILVER_SHOVEL, 1));

        assertThat(ItemListResponse.of(captures).itemList()).extracting(ItemInfo::korName).containsExactly("종이박스", "서류가방", "출장용캐리어");
        assertThat(ItemListResponse.of(traps).itemList()).extracting(ItemInfo::korName).containsExactly("나무삽", "은삽", "금삽");
    }

    @Test
    void 상점_응답은_네_종류_목록을_조합한다() {
        StoreItemResponse response = StoreItemResponse.of(
                List.of(ItemName.WOOD_SHOVEL, ItemName.SILVER_SHOVEL), 0,
                List.of(ItemName.PAPER_BOX), 0,
                List.of(Coin.values()),
                List.of(VacationTicket.values()), false);

        assertThat(response.trapItemList()).hasSize(2);
        assertThat(response.captureItemList()).hasSize(1);
        assertThat(response.coinItemInfoList()).hasSize(3);
        assertThat(response.vacationTicketList()).hasSize(4);
    }
}
