package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.*;
import com.game.subwayworkerserver.entity.FriendEntity;
import lombok.Builder;

public record CapturedFriendInfo(
        String friendName,
        String position,
        String baseHpRange,
        String baseManaRange,
        String baseMaxManaRange,
        String baseAttackRange,
        String baseDefenseRange,
        String baseManaRecoveryRange,
        long hp,
        long mana,
        long maxMana,
        long attack,
        long defense,
        long manaRecovery,
        String activeSkills,
        String passiveSkills
) {

    @Builder
    public CapturedFriendInfo {
    }

    public static CapturedFriendInfo of(final FriendEntity friend, String position) {
        FriendType type = friend.getFriendType();
        return CapturedFriendInfo.builder()
                .friendName(friend.getName().getKoreanName())
                .position(position)
                .baseHpRange(type.getHpRange().min() + "~"  + type.getHpRange().max())
                .baseManaRange(type.getMpNowRange().min() + "~"  + type.getMpNowRange().max())
                .baseMaxManaRange(type.getMpMaxRange().min() + "~"  + type.getMpMaxRange().max())
                .baseAttackRange(type.getAttackRange().min() + "~" + type.getAttackRange().max())
                .baseDefenseRange(type.getDefenseRange().min() + "~" + type.getDefenseRange().max())
                .baseManaRecoveryRange(String.valueOf(type.getManaRecovery()))
                .hp(friend.getHp())
                .mana(friend.getMana())
                .maxMana(friend.getMaxMana())
                .attack(friend.getAttack())
                .defense(friend.getDefense())
                .manaRecovery(friend.getManaRecovery())
                .activeSkills(ActiveSkillCard.of(friend.getActiveSkill1()) + "," + ActiveSkillCard.of(friend.getActiveSkill2()))
                .passiveSkills(PassiveSkillCard.of(friend.getPassiveSkill1()) + "," + PassiveSkillCard.of(friend.getPassiveSkill2()))
                .build();
    }
}
