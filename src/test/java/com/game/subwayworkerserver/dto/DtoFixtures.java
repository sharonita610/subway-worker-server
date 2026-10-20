package com.game.subwayworkerserver.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerElement;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/** DTO 팩토리 테스트가 공유하는 입력 객체. 값은 전부 고정이라 테스트마다 같은 결과가 나온다. */
public final class DtoFixtures {

    private DtoFixtures() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /** private 중첩 record까지 읽을 수 있도록 JSON 트리로 바꾼다. 실제 응답 직렬화와 같은 경로. */
    public static JsonNode json(Object value) {
        return MAPPER.valueToTree(value);
    }

    /** 뭉실(MOONGSHIL) 사원 레벨, 성과 속성, 액티브 201/202, 패시브 101/105 */
    public static CoworkerEntity coworker(long id) {
        CoworkerEntity coworker = CoworkerEntity.builder()
                .name("뭉실").level(CoworkerLevel.WORKER).currentExp(50).totalExp(150).userId(1L)
                .coworkerType(CoworkerType.MOONGSHIL)
                .hp(70).initMana(3).maxMana(30).manaRecovery(10).attack(18).defense(5)
                .inUse(true).onSale(false).available(true).isMaterial(false)
                .activeSkill1(201).activeSkill2(202).passiveSkill1(101).passiveSkill2(105)
                .coworkerElement(CoworkerElement.PERFORMANCE)
                .build();
        ReflectionTestUtils.setField(coworker, "id", id);
        return coworker;
    }

    public static SkillCardEntity skillCard(long id, long coworkerId, SkillCardType type, int enumId, boolean inUse) {
        String engName = type == SkillCardType.ACTIVE ? ActiveSkillCard.from(enumId).name() : PassiveSkillCard.from(enumId).name();
        String korName = type == SkillCardType.ACTIVE ? ActiveSkillCard.from(enumId).getKoreanName() : PassiveSkillCard.from(enumId).getKoreanName();
        SkillCardEntity card = SkillCardEntity.builder()
                .userId(1L).engName(engName).korName(korName).type(type)
                .currentLevel(2).requiredCount(3).count(1).skillCardEnumId(enumId)
                .inUse(inUse).onSale(false).available(true).coworkerId(coworkerId).accumulateCount(1)
                .build();
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }

    public static UserGachaEntity treasure(GachaTreasure gacha, int count) {
        return UserGachaEntity.builder().userId(1L).gacha(gacha).gachaCount(count).build();
    }

    /** exp 40, level 3, totalExp 640, coin 1200, vacation 7, gacha 2, stage 4 */
    public static UserEntity user() {
        UserEntity user = new UserEntity("tester", "uuid-1", 40, 3, 640, 1200, 7, 2, 4, Gender.FEMALE);
        ReflectionTestUtils.setField(user, "id", 9L);
        return user;
    }

    public static CoworkerOnSale coworkerOnSale() {
        return CoworkerOnSale.builder()
                .marketId(5L).coworkerId(10L).engName("MOONGSHIL").korName("뭉실")
                .inUse(true).onSale(true).isMaterial(false)
                .level(2).exp(50).totalExp(150).position("사원").korElement("성과").engElement("PERFORMANCE")
                .hp(70).attack(18).defense(5).initMana(3).maxMana(30).manaRecovery(10)
                .activeSkillId1(201).activeSkillId2(202).passiveSkillId1(101).passiveSkillId2(105)
                .activeSkills(List.of()).passiveSkills(List.of())
                .price(300).sellerId(7L).isPaid(false).isSold(false).type(CoworkerType.MOONGSHIL)
                .build();
    }

    public static SkillCardOnSale skillCardOnSale() {
        return SkillCardOnSale.builder()
                .marketId(5L).cardId(11L).engName("CRITICAL_HIT").korName("회심의 일격").description("설명")
                .inUse(false).onSale(true).available(true)
                .currentLevel(2).requiredCount(3).count(1).skillCardEnumId(206)
                .price(40).sellerId(7L).isPaid(false).isSold(false).cardType("ACTIVE")
                .build();
    }
}
