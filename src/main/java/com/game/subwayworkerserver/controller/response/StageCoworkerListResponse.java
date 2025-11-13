package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record StageCoworkerListResponse(
        List<StageCoworkerInfo> coworkerList
) {

    public static StageCoworkerListResponse of(
            List<CoworkerEntity> coworkerEntityList,
            List<UserGachaEntity> treasureList,
            List<SkillCardEntity> skillCardList
    ) {
        List<StageCoworkerInfo> list = coworkerEntityList.stream()
                .map(entity -> StageCoworkerInfo.of(entity, treasureList, skillCardList))
                .toList();
        return new StageCoworkerListResponse(list);
    }

    private record StageCoworkerInfo(
            long id,
            String engName,
            String korName,
            String position,
            String engElement,
            String korElement,
            boolean isPartner,
            HpInfo hp,
            MpInfo mp,
            AttackInfo attack,
            DefenseInfo defense,
            StagePassiveSkill passiveSkill,
            StageActiveSkill activeSkill,
            ResurrectionInfo resurrection
    ) {
        @Builder
        private StageCoworkerInfo {
        }

        public static StageCoworkerInfo of(
                CoworkerEntity coworker, List<UserGachaEntity> treasureList, List<SkillCardEntity> skillCardList
        ) {
            long coworkerId = coworker.getId();

            SkillCardEntity equippedPassive = skillCardList.stream()
                    .filter(e -> Objects.equals(e.getCoworkerId(), coworkerId))
                    .filter(e -> e.getType() == SkillCardType.PASSIVE)
                    .filter(SkillCardEntity::isInUse)
                    .findFirst()
                    .orElse(null);

            SkillCardEntity equippedActive = skillCardList.stream()
                    .filter(e -> Objects.equals(e.getCoworkerId(), coworkerId))
                    .filter(e -> e.getType() == SkillCardType.ACTIVE)
                    .filter(SkillCardEntity::isInUse)
                    .findFirst()
                    .orElse(null);

            Map<GachaTreasure, Long> bonusMap = treasureList.stream()
                    .collect(Collectors.groupingBy(
                            UserGachaEntity::getGacha,
                            Collectors.summingLong(UserGachaEntity::getGachaCount)
                    ));

            long hpBonus = bonusMap.getOrDefault(GachaTreasure.HP_INCREASE, 0L);
            long attackBonus = bonusMap.getOrDefault(GachaTreasure.ATTACK_UP, 0L);
            long defenseBonus = bonusMap.getOrDefault(GachaTreasure.DEFENSE_UP, 0L);
            long manaRecoveryBonus = bonusMap.getOrDefault(GachaTreasure.MP_RECOVERY_UP, 0L);

            assert equippedPassive != null;
            assert equippedActive != null;
            return StageCoworkerInfo.builder()
                    .id(coworker.getId())
                    .engName(String.valueOf(coworker.getCoworkerType()))
                    .korName(coworker.getName())
                    .position(coworker.getLevel().getTitle())
                    .engElement(coworker.getCoworkerElement().name())
                    .korElement(coworker.getCoworkerElement().getKoreanName())
                    .isPartner(coworker.isInUse())
                    .hp(HpInfo.of(coworker.getHp(), hpBonus, defenseBonus))
                    .mp(MpInfo.of(coworker.getInitMana(), coworker.getMaxMana(), coworker.getManaRecovery(), manaRecoveryBonus))
                    .attack(AttackInfo.of(coworker.getAttack(), attackBonus))
                    .defense(DefenseInfo.of(coworker.getDefense()))
                    .passiveSkill(StagePassiveSkill.of(equippedPassive))
                    .activeSkill(StageActiveSkill.of(equippedActive))
                    .resurrection(ResurrectionInfo.of(equippedPassive.getSkillCardEnumId()))
                    .build();
        }


        private record HpInfo(
                long nowHp, long maxHp, long hpRecover, long shield, long armor
        ) {

            public static HpInfo of(long hp, long hpBonus, long defenseBonus) {
                return new HpInfo( hp + hpBonus, 0, 0, 0, defenseBonus);
            }

        }

        private record MpInfo(
                long nowMp, long maxMp, long mpRecover
        ) {
            public static MpInfo of(long initMana, long maxMana, long manaRecovery, long manaRecoveryBonus) {
                return new MpInfo(initMana, maxMana, manaRecovery + manaRecoveryBonus);
            }
        }

        private record AttackInfo(
                boolean isGlobal, long value
        ) {
            public static AttackInfo of(long coworkerAttack, long attack) {
                // global 은 어케암?
                return new AttackInfo(false, coworkerAttack + attack);
            }
        }

        private record DefenseInfo(
                long defense
        ) {
            public static DefenseInfo of(long defense) {
                return new DefenseInfo(defense);
            }
        }
    }

    private record StagePassiveSkill(
            long id,
            String korName,
            String engName,
            String description,
            int level,
            String targetProperty,
            CountInfo count,
            double effectRate
    ) {

        public static StagePassiveSkill of(SkillCardEntity equippedPassive) {

            PassiveSkillCard card = PassiveSkillCard.from(equippedPassive.getSkillCardEnumId());

            return new StagePassiveSkill(
                    equippedPassive.getId(),
                    equippedPassive.getKorName(),
                    equippedPassive.getEngName(),
                    card.getDescription(),
                    equippedPassive.getCurrentLevel(),
                    card.getProperty(),
                    new CountInfo(1,1),
                    card.getFinalEffectValue(equippedPassive.getCurrentLevel())
            );
        }
    }

    private record CountInfo(
            long now, long max
    ) {
    }

    private record StageActiveSkill(
            long id,
            String korName,
            String engName,
            String description,
            int level,
            CountInfo count,
            int cost,
            String targetProperty,
            double effectRate
    ) {
        public static StageActiveSkill of(SkillCardEntity equippedActive) {
            ActiveSkillCard card = ActiveSkillCard.from(equippedActive.getSkillCardEnumId());

            return new StageActiveSkill(
                    equippedActive.getId(),
                    equippedActive.getKorName(),
                    equippedActive.getEngName(),
                    card.getDescription(),
                    equippedActive.getCurrentLevel(),
                    new CountInfo(1,1),
                    card.getManaCost(),
                    card.getProperty(),
                    card.getFinalEffectValue(equippedActive.getCurrentLevel())
            );
        }
    }


    private record ResurrectionInfo(
            boolean hasResurrection,
            int resurrectionTurn
    ) {
        public static ResurrectionInfo of(int passiveCardEnumId) {

            PassiveSkillCard card = PassiveSkillCard.from(passiveCardEnumId);
            if (card.name().equals(String.valueOf(PassiveSkillCard.SECOND_CHANCE))){
                return new ResurrectionInfo(true, 0);
            }
            return new ResurrectionInfo(false, 0);
        }
    }

}
