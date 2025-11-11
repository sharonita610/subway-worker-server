package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
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
            boolean onSale,
            ExpInfo exp,
            StatsInfos statList,
            EnabledSkills enabledSkills,
            SkillBaseInfo passiveSkillCard,
            SkillBaseInfo activeSkillCard
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
                    .findFirst() // 하나만 가능하므로 findFirst() 사용
                    .orElse(null);

            // 2. 해당 Coworker의 ID를 가지며, 장착된 액티브 스킬 카드를 찾습니다.
            SkillCardEntity equippedActive = skillCardList.stream()
                    .filter(e -> Objects.equals(e.getCoworkerId(), coworkerId))
                    .filter(e -> e.getType() == SkillCardType.ACTIVE)
                    .filter(SkillCardEntity::isInUse)
                    .findFirst() // 하나만 가능하므로 findFirst() 사용
                    .orElse(null);

            // 3. SkillBaseInfo로 변환 (없으면 null)
            SkillBaseInfo passiveInfo = equippedPassive != null ?
                    SkillBaseInfo.of(equippedPassive, SkillCardType.PASSIVE) : null;

            SkillBaseInfo activeInfo = equippedActive != null ?
                    SkillBaseInfo.of(equippedActive, SkillCardType.ACTIVE) : null;

            return StageCoworkerInfo.builder()
                    .id(coworker.getId())
                    .engName(String.valueOf(coworker.getCoworkerType()))
                    .korName(coworker.getName())
                    .position(coworker.getLevel().getTitle())
                    .engElement(coworker.getCoworkerElement().name())
                    .korElement(coworker.getCoworkerElement().getKoreanName())
                    .isPartner(coworker.isInUse())
                    .onSale(coworker.isOnSale())
                    .exp(ExpInfo.of(coworker))
                    .statList(StatsInfos.of(coworker, treasureList))
                    .enabledSkills(EnabledSkills.of(coworker))
                    .passiveSkillCard(passiveInfo)
                    .activeSkillCard(activeInfo)
                    .build();
        }
    }


    private record ExpInfo(
            int level, int now, int max, int total
    ) {

        public static ExpInfo of(CoworkerEntity coworker) {
            return new ExpInfo(
                    coworker.getLevel().getLevel(),
                    coworker.getCurrentExp(),
                    coworker.getLevel().getRequiredExpToNextLevel(),
                    coworker.getTotalExp()
            );
        }
    }

    private record StatBaseInfo(
            long defaultPower, long treasure, long total
    ) {

        public static StatBaseInfo of(long defaultPower, long treasure) {
            return new StatBaseInfo(defaultPower, treasure, defaultPower + treasure);
        }
    }

    private record StatsInfos(
            StatBaseInfo hp,
            StatBaseInfo initMana,
            StatBaseInfo maxMana,
            StatBaseInfo attack,
            StatBaseInfo defense,
            StatBaseInfo manaRecovery
    ) {
        public static StatsInfos of(final CoworkerEntity coworker, List<UserGachaEntity> userGachaEntities) {

            Map<GachaTreasure, Long> bonusMap = userGachaEntities.stream()
                    .collect(Collectors.groupingBy(
                            UserGachaEntity::getGacha,
                            Collectors.summingLong(UserGachaEntity::getGachaCount)
                    ));

            long hpBonus = bonusMap.getOrDefault(GachaTreasure.HP_INCREASE, 0L);
            long attackBonus = bonusMap.getOrDefault(GachaTreasure.ATTACK_UP, 0L);
            long defenseBonus = bonusMap.getOrDefault(GachaTreasure.DEFENSE_UP, 0L);
            long manaRecoveryBonus = bonusMap.getOrDefault(GachaTreasure.MP_RECOVERY_UP, 0L);


            StatBaseInfo hpInfo = StatBaseInfo.of(coworker.getHp(), hpBonus);
            StatBaseInfo manaInfo = StatBaseInfo.of(coworker.getInitMana(), 0L);
            StatBaseInfo maxManaInfo = StatBaseInfo.of(coworker.getMaxMana(), 0L);
            StatBaseInfo attackInfo = StatBaseInfo.of(coworker.getAttack(), attackBonus);
            StatBaseInfo defenseInfo = StatBaseInfo.of(coworker.getDefense(), defenseBonus);
            StatBaseInfo manaRecoveryInfo = StatBaseInfo.of(coworker.getManaRecovery(), manaRecoveryBonus);

            return new StatsInfos(hpInfo, manaInfo, maxManaInfo, attackInfo, defenseInfo, manaRecoveryInfo);
        }
    }
}
