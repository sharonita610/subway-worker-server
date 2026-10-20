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
import static org.assertj.core.api.Assertions.assertThat;

class SkillResponseDtoTest {

    @Test
    void activeSkill은_id_두_개를_enum_정보로_풀고_0은_NONE이_된다() {
        List<SkillBaseInfo> skills = SkillBaseInfo.activeSkill(201, 0);

        assertThat(skills).hasSize(2);
        assertThat(skills.get(0).id()).isEqualTo(201L);
        assertThat(skills.get(0).engName()).isEqualTo("FATIGUE_RELIEVER");
        assertThat(skills.get(0).korName()).isEqualTo("피로회복제 복용");
        assertThat(skills.get(0).description()).isEqualTo(ActiveSkillCard.FATIGUE_RELIEVER.getDescription());
        assertThat(skills.get(0).level()).isZero();
        assertThat(skills.get(1).engName()).isEqualTo("NONE");
    }

    @Test
    void passiveSkill도_같은_방식으로_푼다() {
        List<SkillBaseInfo> skills = SkillBaseInfo.passiveSkill(101, 105);

        assertThat(skills).extracting(SkillBaseInfo::engName).containsExactly("PEACE_OF_MIND", "SECOND_CHANCE");
        assertThat(skills.get(1).description()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getDescription());
    }

    @Test
    void SkillBaseInfo_of는_카드_엔티티와_타입으로_설명과_레벨을_채운다() {
        SkillCardEntity passive = skillCard(2L, 10L, SkillCardType.PASSIVE, 105, true);
        SkillCardEntity active = skillCard(3L, 10L, SkillCardType.ACTIVE, 206, true);

        SkillBaseInfo fromPassive = SkillBaseInfo.of(passive, SkillCardType.PASSIVE);
        SkillBaseInfo fromActive = SkillBaseInfo.of(active, SkillCardType.ACTIVE);

        assertThat(fromPassive.id()).isEqualTo(2L);
        assertThat(fromPassive.description()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getDescription());
        assertThat(fromPassive.level()).isEqualTo(2);
        assertThat(fromActive.description()).isEqualTo(ActiveSkillCard.CRITICAL_HIT.getDescription());
    }

    @Test
    void UpgradedSkillCardResponse는_강화된_카드_하나를_담는다() {
        SkillCardEntity card = skillCard(3L, 10L, SkillCardType.ACTIVE, 206, true);

        SkillCard upgraded = UpgradedSkillCardResponse.of(card).skillCard();

        assertThat(upgraded.id()).isEqualTo(3L);
        assertThat(upgraded.engName()).isEqualTo("CRITICAL_HIT");
        assertThat(upgraded.description()).isEqualTo(ActiveSkillCard.CRITICAL_HIT.getDescription());
        assertThat(upgraded.level()).isEqualTo(2);
        assertThat(upgraded.requiredCount()).isEqualTo(3);
        assertThat(upgraded.actualCount()).isEqualTo(1);
        assertThat(upgraded.inUse()).isTrue();
        assertThat(upgraded.coworkerId()).isEqualTo(10L);
        assertThat(upgraded.cardType()).isEqualTo("ACTIVE");
    }

    @Test
    void EarnedSkillCardResponse는_뽑은_카드들의_요약을_낸다() {
        JsonNode node = json(EarnedSkillCardResponse.of(List.of(skillCard(5L, 0L, SkillCardType.PASSIVE, 101, false))));

        assertThat(node.at("/skillCardList").size()).isEqualTo(1);
        assertThat(node.at("/skillCardList/0/engName").asText()).isEqualTo("PEACE_OF_MIND");
        assertThat(node.at("/skillCardList/0/description").asText()).isEqualTo(PassiveSkillCard.PEACE_OF_MIND.getDescription());
        assertThat(node.at("/skillCardList/0/requiredCount").asInt()).isEqualTo(3);
        assertThat(node.at("/skillCardList/0/actualCount").asInt()).isEqualTo(1);
        assertThat(node.at("/skillCardList/0/cardType").asText()).isEqualTo("PASSIVE");
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
        assertThat(result.get(1).thisCoworkerUsing()).isFalse();
        assertThat(result.get(1).otherCoworkerUsing()).isTrue();
        assertThat(result.get(2).thisCoworkerUsing()).isFalse();
        assertThat(result.get(2).otherCoworkerUsing()).isFalse();
    }

    @Test
    void 사용자_스킬_목록_응답들은_카드를_타입별로_나눠_담는다() {
        List<SkillCardEntity> actives = List.of(skillCard(1L, 0L, SkillCardType.ACTIVE, 201, false));
        List<SkillCardEntity> passives = List.of(skillCard(2L, 0L, SkillCardType.PASSIVE, 101, false), skillCard(3L, 0L, SkillCardType.PASSIVE, 104, false));

        SkillByUserResponse byUser = SkillByUserResponse.of(actives, passives);
        SkillListBySkillIdResponse bySkill = SkillListBySkillIdResponse.of(passives);

        assertThat(byUser.activeSkillCardList()).hasSize(1);
        assertThat(byUser.passiveSkillCardList()).hasSize(2);
        assertThat(bySkill.skillCardList()).extracting(SkillCard::engName).containsExactly("PEACE_OF_MIND", "SHIELD");
    }
}
