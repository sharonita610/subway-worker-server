package com.game.subwayworkerserver.dto.response.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import com.game.subwayworkerserver.dto.response.common.RewardInfo;
import com.game.subwayworkerserver.dto.response.gps.RandomGpsResponse.RandomCoworkerLocationResponse;
import com.game.subwayworkerserver.entity.UserTrapMapEntity;
import com.game.subwayworkerserver.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GpsResponseDtoTest {

    @Test
    void 주변_동료_응답은_한글_이름으로_직업_영문명을_찾는다() {
        RandomCoworkerLocationResponse response = RandomCoworkerLocationResponse.of("c-1", "뭉실", 2, "사원", 37.5, 127.0, 0.3);

        assertThat(response.coworkerId()).isEqualTo("c-1");
        assertThat(response.engName()).isEqualTo("MOONGSHIL");
        assertThat(response.korName()).isEqualTo("뭉실");
        assertThat(response.coworkerLevel()).isEqualTo(2);
        assertThat(response.latitude()).isEqualTo(37.5);
        assertThat(response.longitude()).isEqualTo(127.0);
        assertThat(response.distanceKm()).isEqualTo(0.3);
    }

    @Test
    void 모르는_동료_이름이면_예외가_난다() {
        assertThatThrownBy(() -> RandomCoworkerLocationResponse.of("c-1", "없는이름", 1, "사원", 0, 0, 0.0))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 선물상자_응답은_기본_보상과_보너스_보상을_각각_푼다() {
        JsonNode node = json(GpsRandomGiftResponse.of(
                List.of(new DefaultGift("coin", "코인", 100)),
                List.of(new ExtraGift("WOOD_SHOVEL", 1), new ExtraGift("GACHA_TICKET", 1))));

        assertThat(node.at("/randomGiftList/0/name").asText()).isEqualTo("coin");
        assertThat(node.at("/randomGiftList/0/koreanName").asText()).isEqualTo("코인");
        assertThat(node.at("/randomGiftList/0/quantity").asInt()).isEqualTo(100);
        assertThat(node.at("/bonusGiftInfo/0/name").asText()).isEqualTo("WOOD_SHOVEL");
        assertThat(node.at("/bonusGiftInfo/0/koreanName").asText()).isEqualTo("나무삽");
        assertThat(node.at("/bonusGiftInfo/0/description").asText()).isEqualTo(ItemName.WOOD_SHOVEL.getDescription());
        assertThat(node.at("/bonusGiftInfo/1/name").asText()).isEqualTo("GACHA_TICKET");
        assertThat(node.at("/bonusGiftInfo/1/koreanName").asText()).isEqualTo("가챠티켓");
    }

    @Test
    void 내_트랩_목록은_도구_이름과_위치_만료시각을_담는다() {
        LocalDateTime expires = LocalDateTime.of(2026, 10, 21, 9, 30);
        UserTrapMapEntity trap = new UserTrapMapEntity(1L, ItemName.WOOD_SHOVEL, 37.5, 127.0, expires);

        JsonNode node = json(UserTrapListResponse.of(List.of(trap)));

        assertThat(node.at("/trapList/0/engName").asText()).isEqualTo("WOOD_SHOVEL");
        assertThat(node.at("/trapList/0/korName").asText()).isEqualTo("나무삽");
        assertThat(node.at("/trapList/0/latitude").asDouble()).isEqualTo(37.5);
        assertThat(node.at("/trapList/0/longitude").asDouble()).isEqualTo(127.0);
        assertThat(node.at("/trapList/0/expireTime").isMissingNode()).isFalse();
    }

    @Test
    void RewardInfo와_ItemLocationResponse는_값을_그대로_담는다() {
        assertThat(RewardInfo.of("COIN", 5).quantity()).isEqualTo(5);
        assertThat(new RandomGpsResponse.ItemLocationResponse("giftbox", 37.5, 127.0, 0.1).itemName()).isEqualTo("giftbox");
    }
}
