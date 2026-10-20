package com.game.subwayworkerserver.dto.response.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.reward.DungeonReward;
import com.game.subwayworkerserver.domain.stage.StageMonsterData;
import com.game.subwayworkerserver.domain.stage.StageRewardInfo;
import org.junit.jupiter.api.Test;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;

class StageResponseDtoTest {

    private static StageRewardInfo reward(StageRewardInfo.ExtraReward extra) {
        return StageRewardInfo.builder()
                .userExp(30).coworkerExp(10).coworkersTotalExp(20).coin(1000).gachaTicket(1).vacationTicket(2)
                .firstClear(true).extraReward(extra).build();
    }

    @Test
    void 스테이지_클리어_응답은_보상과_추가_보상을_옮긴다() {
        StageClearResponse response = StageClearResponse.of(reward(new StageRewardInfo.ExtraReward("HP_INCREASE", 1)));

        assertThat(response.userExp()).isEqualTo(30);
        assertThat(response.coworkerExp()).isEqualTo(10);
        assertThat(response.coin()).isEqualTo(1000);
        assertThat(response.gachaTicket()).isEqualTo(1);
        assertThat(response.vacationTicket()).isEqualTo(2);
        assertThat(response.firstClear()).isTrue();
        assertThat(response.extraReward()).isEqualTo(new StageClearResponse.ExtraReward("HP_INCREASE", 1));
    }

    @Test
    void 추가_보상이_없으면_JSON에서_빠진다() {
        JsonNode node = json(StageClearResponse.of(reward(null)));
        JsonNode empty = json(StageClearResponse.empty());

        assertThat(node.has("extraReward")).isFalse();
        assertThat(empty.get("userExp").asInt()).isZero();
        assertThat(empty.has("firstClear")).isFalse();
        assertThat(empty.has("extraReward")).isFalse();
    }

    @Test
    void 던전_보상은_enum_값을_그대로_옮기고_최초_클리어_표시는_없다() {
        StageClearResponse response = StageClearResponse.of(DungeonReward.SUCCESS_REWARD);

        assertThat(response.userExp()).isEqualTo(100);
        assertThat(response.coworkerExp()).isEqualTo(50);
        assertThat(response.coin()).isEqualTo(30000);
        assertThat(response.gachaTicket()).isEqualTo(2);
        assertThat(response.vacationTicket()).isEqualTo(20);
        assertThat(response.firstClear()).isNull();
    }

    @Test
    void 던전_응답은_uuid가_없으면_빈_문자열이다() {
        assertThat(DungeonResponse.of(true)).isEqualTo(new DungeonResponse(true, ""));
        assertThat(DungeonResponse.of(false, "d-1")).isEqualTo(new DungeonResponse(false, "d-1"));
    }

    @Test
    void 몬스터_응답은_목록을_그대로_담는다() {
        assertThat(new MonsterResponse(StageMonsterData.forStage(1)).monsters()).hasSize(3);
    }
}
