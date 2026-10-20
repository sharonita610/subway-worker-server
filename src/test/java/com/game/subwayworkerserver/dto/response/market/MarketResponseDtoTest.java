package com.game.subwayworkerserver.dto.response.market;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.coworkerOnSale;
import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static com.game.subwayworkerserver.dto.DtoFixtures.skillCardOnSale;
import static org.assertj.core.api.Assertions.assertThat;

class MarketResponseDtoTest {

    @Test
    void 장터_스킬카드는_가격_정보와_카드_정보로_나뉜다() {
        MarketSkillCardInfo info = MarketSkillCardInfo.of(skillCardOnSale());
        JsonNode node = json(info);

        assertThat(info.price()).isEqualTo(40);
        assertThat(info.sellerId()).isEqualTo(7L);
        assertThat(info.marketId()).isEqualTo(5L);
        assertThat(node.at("/skillCardBaseInfo/id").asLong()).isEqualTo(11L);
        assertThat(node.at("/skillCardBaseInfo/korName").asText()).isEqualTo("회심의 일격");
        assertThat(node.at("/skillCardBaseInfo/cardType").asText()).isEqualTo("ACTIVE");
        assertThat(node.at("/skillCardBaseInfo/level").asInt()).isEqualTo(2);
        assertThat(node.at("/skillCardBaseInfo/skillCardEnumId").asInt()).isEqualTo(206);
    }

    @Test
    void 전역_장터_동료는_판매자가_붙고_정산_상태는_빠진다() {
        JsonNode node = json(MarketCoworkerListResponse.onSale(List.of(coworkerOnSale()))).at("/coworkerList/0");

        assertThat(node.get("price").asInt()).isEqualTo(300);
        assertThat(node.get("sellerId").asLong()).isEqualTo(7L);
        assertThat(node.get("marketId").asLong()).isEqualTo(5L);
        assertThat(node.has("isPaid")).isFalse();
        assertThat(node.has("isSold")).isFalse();
        assertThat(node.at("/coworkerBaseInfo/korName").asText()).isEqualTo("뭉실");
    }

    @Test
    void 내_판매_동료는_정산_상태가_붙고_판매자는_빠진다() {
        JsonNode node = json(MarketCoworkerListResponse.mine(List.of(coworkerOnSale()))).at("/coworkerList/0");

        assertThat(node.get("price").asInt()).isEqualTo(300);
        assertThat(node.has("sellerId")).isFalse();
        assertThat(node.get("isPaid").asBoolean()).isFalse();
        assertThat(node.get("isSold").asBoolean()).isFalse();
        assertThat(node.at("/coworkerBaseInfo/id").asLong()).isEqualTo(10L);
    }

    @Test
    void 목록_응답들은_입력_개수만큼_항목을_만든다() {
        assertThat(MarketSkillCardListResponse.of(List.of(skillCardOnSale(), skillCardOnSale())).skillCardList()).hasSize(2);
        assertThat(MarketCoworkerListResponse.onSale(List.of()).coworkerList()).isEmpty();
        assertThat(MarketCoworkerListResponse.mine(List.of(coworkerOnSale())).coworkerList()).hasSize(1);
    }

    @Test
    void PaymentSuccessResponse는_판매가_수수료_실수령액을_담는다() {
        PaymentSuccessResponse response = PaymentSuccessResponse.of(100, 10, 90);

        assertThat(response.sellingPrice()).isEqualTo(100);
        assertThat(response.commissionPrice()).isEqualTo(10);
        assertThat(response.profit()).isEqualTo(90);
    }
}
