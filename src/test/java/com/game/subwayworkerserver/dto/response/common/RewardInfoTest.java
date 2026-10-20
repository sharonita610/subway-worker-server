package com.game.subwayworkerserver.dto.response.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.item.ItemName;
import org.junit.jupiter.api.Test;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;

class RewardInfoTest {

    @Test
    void 이름과_수량만_있으면_나머지_키는_JSON에서_빠진다() {
        JsonNode node = json(RewardInfo.of("COIN", 5));

        assertThat(node.get("name").asText()).isEqualTo("COIN");
        assertThat(node.get("quantity").asInt()).isEqualTo(5);
        assertThat(node.has("koreanName")).isFalse();
        assertThat(node.has("description")).isFalse();
    }

    @Test
    void 아이템_보상은_상수명으로_한글_이름과_설명을_채운다() {
        assertThat(RewardInfo.ofItem("WOOD_SHOVEL", 1))
                .isEqualTo(new RewardInfo("WOOD_SHOVEL", "나무삽", ItemName.WOOD_SHOVEL.getDescription(), 1));
        assertThat(RewardInfo.ofItem("GACHA_TICKET", 2))
                .isEqualTo(new RewardInfo("GACHA_TICKET", "가챠티켓", "가챠티켓", 2));
        assertThat(RewardInfo.of("coin", "코인", 100).koreanName()).isEqualTo("코인");
    }
}
