package com.game.subwayworkerserver.dto.response.gacha;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.DefaultGift;
import com.game.subwayworkerserver.domain.reward.RandomBoxReward.ExtraGift;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static com.game.subwayworkerserver.dto.DtoFixtures.treasure;
import static com.game.subwayworkerserver.dto.DtoFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

class GachaResponseDtoTest {

    @Test
    void 가챠_결과는_추가_보상을_보물과_아이템으로_나눈다() {
        JsonNode node = json(LuckyGachaEarnResponse.of(
                List.of(new DefaultGift("coin", "코인", 100), new DefaultGift("vacationTicket", "휴가티켓", 20)),
                List.of(new ExtraGift("ATTACK_UP", 2), new ExtraGift("BRIEFCASE", 1), new ExtraGift("MATERIAL_COWORKER", 1))));

        assertThat(node.at("/defaultRewardList/0/name").asText()).isEqualTo("coin");
        assertThat(node.at("/defaultRewardList/0/quantity").asInt()).isEqualTo(100);
        assertThat(node.at("/defaultRewardList").size()).isEqualTo(2);
        assertThat(node.at("/extraRewardList/treasureList/0/name").asText()).isEqualTo("ATTACK_UP");
        assertThat(node.at("/extraRewardList/treasureList/0/quantity").asInt()).isEqualTo(2);
        assertThat(node.at("/extraRewardList/itemList").size()).isEqualTo(2);
        assertThat(node.at("/extraRewardList/itemList/1/name").asText()).isEqualTo("MATERIAL_COWORKER");
    }

    @Test
    void 가챠_목록은_티켓_잔량과_보물_보유량을_담는다() {
        LuckyGachaListResponse response = LuckyGachaListResponse.of(user(), List.of(treasure(GachaTreasure.HP_INCREASE, 3)));

        assertThat(response.gachaTicket()).isEqualTo(2);
        assertThat(response.vacationTicket()).isEqualTo(7);
        assertThat(response.gachaList()).containsExactly(new GachaBaseInfo("HP_INCREASE", "베개", "HP 증가", 3, 20));
    }

    @Test
    void GachaBaseInfo의_최대_보유량은_20이다() {
        assertThat(GachaBaseInfo.of(treasure(GachaTreasure.DEFENSE_UP, 0)).maxCount()).isEqualTo(20);
    }
}
