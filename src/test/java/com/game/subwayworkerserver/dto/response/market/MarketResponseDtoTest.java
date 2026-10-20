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
    void GlobalSkillCardInfo는_장터_스킬카드를_가격_정보와_카드_정보로_나눈다() {
        GlobalSkillCardInfo info = GlobalSkillCardInfo.of(skillCardOnSale());
        JsonNode node = json(info);

        assertThat(info.price()).isEqualTo(40);
        assertThat(info.sellerId()).isEqualTo(7L);
        assertThat(info.marketId()).isEqualTo(5L);
        assertThat(node.at("/skillCardBaseInfo/id").asLong()).isEqualTo(11L);
        assertThat(node.at("/skillCardBaseInfo/korName").asText()).isEqualTo("회심의 일격");
        assertThat(node.at("/skillCardBaseInfo/cardType").asText()).isEqualTo("ACTIVE");
        assertThat(node.at("/skillCardBaseInfo/currentLevel").asInt()).isEqualTo(2);
        assertThat(node.at("/skillCardBaseInfo/count").asInt()).isEqualTo(1);
        assertThat(node.at("/skillCardBaseInfo/requiredCount").asInt()).isEqualTo(3);
        assertThat(node.at("/skillCardBaseInfo/skillCardEnumId").asInt()).isEqualTo(206);
        assertThat(node.at("/skillCardBaseInfo/available").asBoolean()).isTrue();
    }

    @Test
    void GlobalCoworkerInfo는_가격_정보와_동료_기본_정보로_나눈다() {
        GlobalCoworkerInfo info = GlobalCoworkerInfo.of(coworkerOnSale());

        assertThat(info.price()).isEqualTo(300);
        assertThat(info.sellerId()).isEqualTo(7L);
        assertThat(info.marketId()).isEqualTo(5L);
        assertThat(info.coworkerBaseInfo().id()).isEqualTo(10L);
        assertThat(info.coworkerBaseInfo().korName()).isEqualTo("뭉실");
    }

    @Test
    void MyCoworkersMarketResponse는_내_판매_목록을_정산_상태와_함께_낸다() {
        JsonNode node = json(MyCoworkersMarketResponse.of(List.of(coworkerOnSale())));

        assertThat(node.at("/coworkerList").size()).isEqualTo(1);
        assertThat(node.at("/coworkerList/0/price").asInt()).isEqualTo(300);
        assertThat(node.at("/coworkerList/0/marketId").asLong()).isEqualTo(5L);
        assertThat(node.at("/coworkerList/0/coworkerBaseInfo/id").asLong()).isEqualTo(10L);
    }

    @Test
    void 목록_응답들은_입력_개수만큼_항목을_만든다() {
        assertThat(SkillOnSaleResponse.of(List.of(skillCardOnSale(), skillCardOnSale())).skillCardList()).hasSize(2);
        assertThat(GlobalSkillCardResponse.of(List.of(skillCardOnSale())).skillCardList()).hasSize(1);
        assertThat(GlobalCoworkerResponse.of(List.of(coworkerOnSale())).coworkerList()).hasSize(1);
        assertThat(GlobalCoworkerResponse.of(List.of()).coworkerList()).isEmpty();
    }

    @Test
    void PaymentSuccessResponse는_판매가_수수료_실수령액을_담는다() {
        PaymentSuccessResponse response = PaymentSuccessResponse.of(100, 10, 90);

        assertThat(response.sellingPrice()).isEqualTo(100);
        assertThat(response.commissionPrice()).isEqualTo(10);
        assertThat(response.profit()).isEqualTo(90);
    }
}
