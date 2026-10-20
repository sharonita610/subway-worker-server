package com.game.subwayworkerserver.dto.response.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static com.game.subwayworkerserver.dto.DtoFixtures.skillCard;
import static com.game.subwayworkerserver.dto.DtoFixtures.skillCardOnSale;
import static org.assertj.core.api.Assertions.assertThat;

class SkillResponseDtoTest {

    @Test
    void 스킬_정의만_채우면_나머지_키는_JSON에서_빠진다() {
        List<SkillCardInfo> skills = SkillCardInfo.activeSkills(201, 0);
        JsonNode node = json(skills.get(0));

        assertThat(skills).hasSize(2);
        assertThat(skills.get(0).id()).isEqualTo(201L);
        assertThat(skills.get(0).engName()).isEqualTo("FATIGUE_RELIEVER");
        assertThat(skills.get(0).korName()).isEqualTo("피로회복제 복용");
        assertThat(skills.get(0).description()).isEqualTo(ActiveSkillCard.FATIGUE_RELIEVER.getDescription());
        assertThat(skills.get(1).engName()).isEqualTo("NONE");
        assertThat(node.has("level")).isFalse();
        assertThat(node.has("cardType")).isFalse();
        assertThat(node.has("inUse")).isFalse();
    }

    @Test
    void 패시브_스킬도_같은_방식으로_푼다() {
        List<SkillCardInfo> skills = SkillCardInfo.passiveSkills(101, 105);

        assertThat(skills).extracting(SkillCardInfo::engName).containsExactly("PEACE_OF_MIND", "SECOND_CHANCE");
        assertThat(skills.get(1).description()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getDescription());
    }

    @Test
    void summary는_카드_엔티티의_정의와_레벨만_담는다() {
        SkillCardInfo info = SkillCardInfo.summary(skillCard(2L, 10L, SkillCardType.PASSIVE, 105, true));
        JsonNode node = json(info);

        assertThat(info.id()).isEqualTo(2L);
        assertThat(info.description()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getDescription());
        assertThat(info.level()).isEqualTo(2);
        assertThat(node.has("requiredCount")).isFalse();
    }

    @Test
    void earned는_강화_진행도와_카드_타입을_더한다() {
        JsonNode node = json(EarnedSkillCardResponse.of(List.of(skillCard(5L, 0L, SkillCardType.PASSIVE, 101, false))));
        JsonNode card = node.at("/skillCardList/0");

        assertThat(node.at("/skillCardList").size()).isEqualTo(1);
        assertThat(card.get("engName").asText()).isEqualTo("PEACE_OF_MIND");
        assertThat(card.get("description").asText()).isEqualTo(PassiveSkillCard.PEACE_OF_MIND.getDescription());
        assertThat(card.get("level").asInt()).isEqualTo(2);
        assertThat(card.get("requiredCount").asInt()).isEqualTo(3);
        assertThat(card.get("actualCount").asInt()).isEqualTo(1);
        assertThat(card.get("cardType").asText()).isEqualTo("PASSIVE");
        assertThat(card.has("id")).isFalse();
        assertThat(card.has("isPaid")).isFalse();
    }

    @Test
    void onSale은_거래_상태까지_전부_담는다() {
        JsonNode node = json(SkillCardInfo.onSale(skillCardOnSale()));

        assertThat(node.get("id").asLong()).isEqualTo(11L);
        assertThat(node.get("korName").asText()).isEqualTo("회심의 일격");
        assertThat(node.get("level").asInt()).isEqualTo(2);
        assertThat(node.get("actualCount").asInt()).isEqualTo(1);
        assertThat(node.get("requiredCount").asInt()).isEqualTo(3);
        assertThat(node.get("skillCardEnumId").asInt()).isEqualTo(206);
        assertThat(node.get("cardType").asText()).isEqualTo("ACTIVE");
        assertThat(node.get("available").asBoolean()).isTrue();
        assertThat(node.get("onSale").asBoolean()).isTrue();
        assertThat(node.get("isPaid").asBoolean()).isFalse();
        assertThat(node.get("isSold").asBoolean()).isFalse();
    }

    @Test
    void UpgradedSkillCardResponse는_강화된_카드_하나를_담는다() {
        SkillCardEntity card = skillCard(3L, 10L, SkillCardType.ACTIVE, 206, true);

        SkillCard upgraded = UpgradedSkillCardResponse.of(card).skillCard();

        assertThat(upgraded.id()).isEqualTo(3L);
        assertThat(upgraded.engName()).isEqualTo("CRITICAL_HIT");
        assertThat(upgraded.description()).isEqualTo(ActiveSkillCard.CRITICAL_HIT.getDescription());
        assertThat(upgraded.level()).isEqualTo(2);
        assertThat(upgraded.inUse()).isTrue();
        assertThat(upgraded.coworkerId()).isEqualTo(10L);
        assertThat(upgraded.cardType()).isEqualTo("ACTIVE");
    }

    @Test
    void SkillByCoworkerResponse는_이_동료와_다른_동료의_사용_여부를_구분한다() {
        List<SkillCardEntity> cards = List.of(
                skillCard(1L, 10L, SkillCardType.ACTIVE, 201, true),
                skillCard(2L, 99L, SkillCardType.ACTIVE, 202, true),
                skillCard(3L, 0L, SkillCardType.ACTIVE, 203, false));

        List<SkillCard> result = SkillByCoworkerResponse.of(cards, 10L).skillCardList();

        assertThat(result.get(0).thisCoworkerUsing()).isTrue();
        assertThat(result.get(0).otherCoworkerUsing()).isFalse();
        assertThat(result.get(1).otherCoworkerUsing()).isTrue();
        assertThat(result.get(2).thisCoworkerUsing()).isFalse();
    }

    @Test
    void 사용자_스킬_목록_응답들은_카드를_타입별로_나눠_담는다() {
        List<SkillCardEntity> actives = List.of(skillCard(1L, 0L, SkillCardType.ACTIVE, 201, false));
        List<SkillCardEntity> passives = List.of(skillCard(2L, 0L, SkillCardType.PASSIVE, 101, false), skillCard(3L, 0L, SkillCardType.PASSIVE, 104, false));

        assertThat(SkillByUserResponse.of(actives, passives).activeSkillCardList()).hasSize(1);
        assertThat(SkillByUserResponse.of(actives, passives).passiveSkillCardList()).hasSize(2);
        assertThat(SkillListBySkillIdResponse.of(passives).skillCardList()).extracting(SkillCard::engName).containsExactly("PEACE_OF_MIND", "SHIELD");
    }
}
