package com.game.subwayworkerserver.dto.response.coworker;

import com.fasterxml.jackson.databind.JsonNode;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.coworker;
import static com.game.subwayworkerserver.dto.DtoFixtures.coworkerOnSale;
import static com.game.subwayworkerserver.dto.DtoFixtures.json;
import static com.game.subwayworkerserver.dto.DtoFixtures.skillCard;
import static com.game.subwayworkerserver.dto.DtoFixtures.treasure;
import static org.assertj.core.api.Assertions.assertThat;

class CoworkerResponseDtoTest {

    private static final List<UserGachaEntity> TREASURES = List.of(
            treasure(GachaTreasure.HP_INCREASE, 3), treasure(GachaTreasure.ATTACK_UP, 2),
            treasure(GachaTreasure.HP_INCREASE, 1), treasure(GachaTreasure.DEFENSE_UP, 2),
            treasure(GachaTreasure.MP_RECOVERY_UP, 5));

    @Nested
    class CoworkerBaseInfo_ {

        @Test
        void fromEntity는_기본_정보와_속성을_그대로_옮긴다() {
            CoworkerBaseInfo info = CoworkerBaseInfo.fromEntity(coworker(10L), List.of(), List.of());

            assertThat(info.summary()).isEqualTo(new CoworkerSummary(10L, "MOONGSHIL", "뭉실", "사원", 2, "PERFORMANCE", "성과"));
            assertThat(info.isPartner()).isTrue();
            assertThat(info.onSale()).isFalse();
            assertThat(info.isMaterial()).isFalse();
        }

        @Test
        void 식별_정보는_JSON에서_중첩_없이_평탄하게_나간다() {
            JsonNode node = json(CoworkerBaseInfo.fromEntity(coworker(10L)));

            assertThat(node.has("summary")).isFalse();
            assertThat(node.get("id").asLong()).isEqualTo(10L);
            assertThat(node.get("korName").asText()).isEqualTo("뭉실");
            assertThat(node.get("level").asInt()).isEqualTo(2);
            assertThat(json(CapturedCoworkerInfo.of(coworker(10L), "사원")).get("korName").asText()).isEqualTo("뭉실");
        }

        @Test
        void exp는_레벨표에서_다음_레벨_필요_경험치를_가져온다() {
            JsonNode node = json(CoworkerBaseInfo.fromEntity(coworker(10L), List.of(), List.of()));

            assertThat(node.at("/exp/level").asInt()).isEqualTo(2);
            assertThat(node.at("/exp/now").asInt()).isEqualTo(50);
            assertThat(node.at("/exp/max").asInt()).isEqualTo(CoworkerLevel.WORKER.getRequiredExpToNextLevel());
            assertThat(node.at("/exp/total").asInt()).isEqualTo(150);
        }

        @Test
        void 스탯은_보물_보너스를_종류별로_합산해_total에_더한다() {
            JsonNode node = json(CoworkerBaseInfo.fromEntity(coworker(10L), TREASURES, List.of()));

            assertThat(node.at("/statList/hp/defaultPower").asLong()).isEqualTo(70);
            assertThat(node.at("/statList/hp/treasure").asLong()).isEqualTo(4);
            assertThat(node.at("/statList/hp/total").asLong()).isEqualTo(74);
            assertThat(node.at("/statList/attack/total").asLong()).isEqualTo(20);
            assertThat(node.at("/statList/defense/total").asLong()).isEqualTo(7);
            assertThat(node.at("/statList/manaRecovery/total").asLong()).isEqualTo(15);
            assertThat(node.at("/statList/initMana/treasure").asLong()).isZero();
            assertThat(node.at("/statList/maxMana/total").asLong()).isEqualTo(30);
        }

        @Test
        void enabledSkills는_동료의_스킬_id_네_개를_enum_정보로_푼다() {
            CoworkerBaseInfo info = CoworkerBaseInfo.fromEntity(coworker(10L), List.of(), List.of());

            assertThat(info.enabledSkills().activeSkills()).extracting("engName").containsExactly("FATIGUE_RELIEVER", "BLOOD_DONATION");
            assertThat(info.enabledSkills().passiveSkills()).extracting("engName").containsExactly("PEACE_OF_MIND", "SECOND_CHANCE");
        }

        @Test
        void selectedSkills는_이_동료가_장착_중인_카드만_담는다() {
            List<SkillCardEntity> cards = List.of(
                    skillCard(1L, 10L, SkillCardType.ACTIVE, 201, true),
                    skillCard(2L, 10L, SkillCardType.PASSIVE, 101, false),
                    skillCard(3L, 99L, SkillCardType.ACTIVE, 202, true));

            CoworkerBaseInfo info = CoworkerBaseInfo.fromEntity(coworker(10L), List.of(), cards);

            assertThat(info.selectedSkills()).hasSize(1);
            var card = info.selectedSkills().get(0);
            assertThat(card.id()).isEqualTo(1L);
            assertThat(card.engName()).isEqualTo("FATIGUE_RELIEVER");
            assertThat(card.description()).isEqualTo(ActiveSkillCard.FATIGUE_RELIEVER.getDescription());
            assertThat(card.level()).isEqualTo(2);
            assertThat(card.requiredCount()).isEqualTo(3);
            assertThat(card.actualCount()).isEqualTo(1);
            assertThat(card.thisCoworkerUsing()).isTrue();
            assertThat(card.otherCoworkerUsing()).isFalse();
            assertThat(card.coworkerId()).isEqualTo(10L);
            assertThat(card.cardType()).isEqualTo("ACTIVE");
        }

        @Test
        void 패시브_카드_설명은_PassiveSkillCard에서_가져온다() {
            List<SkillCardEntity> cards = List.of(skillCard(2L, 10L, SkillCardType.PASSIVE, 105, true));

            CoworkerBaseInfo info = CoworkerBaseInfo.of(coworker(10L), cards);

            assertThat(info.selectedSkills().get(0).description()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getDescription());
        }

        @Test
        void 단일_인자_fromEntity와_of는_보물_보너스_없이_만든다() {
            JsonNode single = json(CoworkerBaseInfo.fromEntity(coworker(10L)));
            JsonNode withSkills = json(CoworkerBaseInfo.of(coworker(10L), List.of()));

            assertThat(single.at("/statList/hp/treasure").asLong()).isZero();
            assertThat(single.at("/selectedSkills").size()).isZero();
            assertThat(withSkills.at("/statList/attack/treasure").asLong()).isZero();
        }

        @Test
        void fromDomain은_장터_도메인_객체를_옮기고_선택_스킬은_비운다() {
            CoworkerBaseInfo info = CoworkerBaseInfo.fromDomain(coworkerOnSale());
            JsonNode node = json(info);

            assertThat(info.summary().id()).isEqualTo(10L);
            assertThat(info.summary().korName()).isEqualTo("뭉실");
            assertThat(info.isPartner()).isTrue();
            assertThat(info.onSale()).isTrue();
            assertThat(node.at("/exp/max").asInt()).isEqualTo(CoworkerLevel.of(2).getRequiredExpToNextLevel());
            assertThat(node.at("/statList/hp/total").asLong()).isEqualTo(70);
            assertThat(info.enabledSkills().activeSkills()).hasSize(2);
            assertThat(info.selectedSkills()).isEmpty();
        }
    }

    @Nested
    class StageCoworkerListResponse_ {

        private final CoworkerEntity coworker = coworker(10L);

        @Test
        void 전투_스탯에_보물_보너스를_반영한다() {
            JsonNode node = json(StageCoworkerListResponse.of(List.of(coworker), TREASURES, List.of()));
            JsonNode info = node.at("/coworkerList/0");

            assertThat(info.get("id").asLong()).isEqualTo(10L);
            assertThat(info.at("/hp/nowHp").asLong()).isEqualTo(74);
            assertThat(info.at("/hp/maxHp").asLong()).isEqualTo(74);
            assertThat(info.at("/hp/hpRecover").asLong()).isZero();
            assertThat(info.at("/hp/armor").asLong()).isEqualTo(2);
            assertThat(info.at("/mp/nowMp").asLong()).isEqualTo(3);
            assertThat(info.at("/mp/maxMp").asLong()).isEqualTo(30);
            assertThat(info.at("/mp/mpRecover").asLong()).isEqualTo(15);
            assertThat(info.at("/attack/value").asLong()).isEqualTo(20);
            assertThat(info.at("/defense/defense").asLong()).isEqualTo(5);
        }

        @Test
        void 장착한_패시브와_액티브_카드를_전투용_정보로_푼다() {
            List<SkillCardEntity> cards = List.of(
                    skillCard(21L, 10L, SkillCardType.PASSIVE, 105, true),
                    skillCard(22L, 10L, SkillCardType.ACTIVE, 206, true),
                    skillCard(23L, 99L, SkillCardType.ACTIVE, 201, true));

            JsonNode info = json(StageCoworkerListResponse.of(List.of(coworker), List.of(), cards)).at("/coworkerList/0");

            assertThat(info.at("/passiveSkill/id").asLong()).isEqualTo(21L);
            assertThat(info.at("/passiveSkill/engName").asText()).isEqualTo("SECOND_CHANCE");
            assertThat(info.at("/passiveSkill/level").asInt()).isEqualTo(2);
            assertThat(info.at("/passiveSkill/targetProperty").asText()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getProperty());
            assertThat(info.at("/passiveSkill/count/now").asLong()).isEqualTo(1);
            assertThat(info.at("/passiveSkill/effectRate").asDouble()).isEqualTo(PassiveSkillCard.SECOND_CHANCE.getFinalEffectValue(2));
            assertThat(info.at("/activeSkill/id").asLong()).isEqualTo(22L);
            assertThat(info.at("/activeSkill/korName").asText()).isEqualTo("회심의 일격");
            assertThat(info.at("/activeSkill/count/now").asLong()).isEqualTo(1);
            assertThat(info.at("/activeSkill/count/max").asLong()).isEqualTo(3);
            assertThat(info.at("/activeSkill/cost").asInt()).isEqualTo(ActiveSkillCard.CRITICAL_HIT.getManaCost());
            assertThat(info.at("/resurrection/hasResurrection").asBoolean()).isTrue();
        }

        @Test
        void 장착_카드가_없으면_빈_스킬_정보와_부활_없음이다() {
            JsonNode info = json(StageCoworkerListResponse.of(List.of(coworker), List.of(), List.of())).at("/coworkerList/0");

            assertThat(info.at("/passiveSkill/id").asLong()).isZero();
            assertThat(info.at("/passiveSkill/engName").asText()).isEmpty();
            assertThat(info.at("/passiveSkill/count/max").asLong()).isZero();
            assertThat(info.at("/activeSkill/cost").asInt()).isZero();
            assertThat(info.at("/activeSkill/targetProperty").asText()).isEmpty();
            assertThat(info.at("/resurrection/hasResurrection").asBoolean()).isFalse();
        }

        @Test
        void 두번째_기회가_아닌_패시브는_부활이_없다() {
            List<SkillCardEntity> cards = List.of(skillCard(21L, 10L, SkillCardType.PASSIVE, 101, true));

            JsonNode info = json(StageCoworkerListResponse.of(List.of(coworker), List.of(), cards)).at("/coworkerList/0");

            assertThat(info.at("/passiveSkill/engName").asText()).isEqualTo("PEACE_OF_MIND");
            assertThat(info.at("/resurrection/hasResurrection").asBoolean()).isFalse();
        }
    }

    @Nested
    class Capture_ {

        @Test
        void CapturedCoworkerInfo는_직업의_스탯_범위를_문자열로_만든다() {
            CapturedCoworkerInfo info = CapturedCoworkerInfo.of(coworker(10L), "사원");

            assertThat(info.summary().korName()).isEqualTo("뭉실");
            assertThat(info.summary().position()).isEqualTo("사원");
            assertThat(info.summary().id()).isEqualTo(10L);
            assertThat(info.baseHpRange()).isEqualTo("58~91");
            assertThat(info.baseInitManaRange()).isEqualTo("0~5");
            assertThat(info.baseMaxManaRange()).isEqualTo("23~36");
            assertThat(info.baseAttackRange()).isEqualTo("14~21");
            assertThat(info.baseDefenseRange()).isEqualTo("3~7");
            assertThat(info.baseManaRecoveryRange()).isEqualTo("10");
            assertThat(info.hp()).isEqualTo(70);
            assertThat(info.activeSkills()).hasSize(2);
            assertThat(info.passiveSkills()).hasSize(2);
            assertThat(info.summary().engElement()).isEqualTo("PERFORMANCE");
            assertThat(info.summary().korElement()).isEqualTo("성과");
        }

        @Test
        void 포획_성공_응답은_보상과_동료_정보를_담는다() {
            CaptureResponse response = CaptureResponse.of(BaseReward.getCaptureReward(), 50, coworker(10L), "사원");

            assertThat(response.success()).isTrue();
            assertThat(response.run()).isFalse();
            assertThat(response.reward()).isEqualTo(BaseReward.getCaptureReward());
            assertThat(response.extraExpReward()).isEqualTo(50);
            assertThat(response.capturedCoworkerInfo().summary().korName()).isEqualTo("뭉실");
        }

        @Test
        void 포획_실패_응답은_실패_보상과_도주_여부만_담는다() {
            CaptureResponse response = CaptureResponse.of(false, true);

            assertThat(response.success()).isFalse();
            assertThat(response.run()).isTrue();
            assertThat(response.reward()).isEqualTo(BaseReward.getFailedCaptureReward());
            assertThat(response.capturedCoworkerInfo()).isNull();
        }

    }

    @Nested
    class Lists_ {

        @Test
        void CoworkerListResponse_of_coin_exp는_보상을_담는다() {
            CoworkerListResponse response = CoworkerListResponse.of(5000, 50, List.of(coworker(10L)));

            assertThat(response.reward()).isEqualTo(new BaseReward(50, 5000));
            assertThat(response.coworkerList()).extracting(info -> info.summary().id()).containsExactly(10L);
        }

        @Test
        void CoworkerListResponse의_보상이_없으면_JSON에서_빠진다() {
            JsonNode empty = json(CoworkerListResponse.empty());
            JsonNode listed = json(CoworkerListResponse.of(List.of(coworker(10L)), List.of(), List.of()));

            assertThat(empty.has("reward")).isFalse();
            assertThat(empty.at("/coworkerList").size()).isZero();
            assertThat(listed.has("reward")).isFalse();
            assertThat(listed.at("/coworkerList/0/id").asLong()).isEqualTo(10L);
        }

        @Test
        void CoworkerTypesResponse는_직업_이름을_영문_한글로_낸다() {
            JsonNode node = json(CoworkerTypesResponse.from(List.of(CoworkerType.MOONGSHIL, CoworkerType.MOKA)));

            assertThat(node.at("/typeNames/0/engName").asText()).isEqualTo("MOONGSHIL");
            assertThat(node.at("/typeNames/0/korName").asText()).isEqualTo("뭉실");
            assertThat(node.at("/typeNames").size()).isEqualTo(2);
        }

        @Test
        void EnabledSkills는_엔티티와_도메인_양쪽에서_같은_결과를_만든다() {
            EnabledSkills fromEntity = EnabledSkills.of(coworker(10L));
            EnabledSkills fromDomain = EnabledSkills.of(coworkerOnSale());

            assertThat(fromEntity).isEqualTo(fromDomain);
            assertThat(fromEntity.activeSkills()).extracting("id").containsExactly(201L, 202L);
            assertThat(fromEntity.passiveSkills()).extracting("id").containsExactly(101L, 105L);
        }

        @Test
        void UserUpgradeResponse는_강화된_동료를_감싼다() {
            UserUpgradeResponse response = UserUpgradeResponse.of(coworker(10L), List.of());

            assertThat(response.coworker().summary().id()).isEqualTo(10L);
            assertThat(response.coworker().selectedSkills()).isEmpty();
        }
    }
}
