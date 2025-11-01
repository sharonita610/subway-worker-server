package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.*;
import com.game.subwayworkerserver.entity.FriendEntity;
import lombok.Builder;

import java.util.List;

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
        List<SkillBaseInfo> activeSkills,
        List<SkillBaseInfo> passiveSkills,
        String engAttributeName,
        String korAttributeName
) {

    @Builder
    public CapturedFriendInfo {
    }

    public static CapturedFriendInfo of(final FriendEntity friend, String position) {
        FriendType type = friend.getFriendType();
        return CapturedFriendInfo.builder()
                .friendName(friend.getName())
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
                .activeSkills(SkillBaseInfo.activeSkill(friend.getActiveSkill1(), friend.getActiveSkill2()))
                .passiveSkills(SkillBaseInfo.passiveSkill(friend.getActiveSkill1(), friend.getPassiveSkill2()))
                .engAttributeName(friend.getFriendAttribute().name())
                .korAttributeName(friend.getFriendAttribute().getKoreanName())
                .build();
    }
}
