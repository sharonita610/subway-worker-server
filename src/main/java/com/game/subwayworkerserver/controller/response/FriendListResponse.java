package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.entity.FriendEntity;

import java.util.List;

public record FriendListResponse(
        List<FriendBaseInfo> friendList
) {
    public static FriendListResponse of(final List<FriendEntity> friendEntityList) {
        List<FriendBaseInfo> friendBaseInfos = friendEntityList.stream()
                .map(friend ->
                        FriendBaseInfo.builder()
                                .id(friend.getId())
                                .inUse(friend.isInUse())
                                .onSale(friend.isOnSale())
                                .hp(friend.getHp())
                                .mana(friend.getMana())
                                .maxMana(friend.getMaxMana())
                                .attack(friend.getAttack())
                                .defense(friend.getDefense())
                                .manaRecovery(friend.getManaRecovery())
                                .activeSkills(SkillBaseInfo.activeSkill(friend.getActiveSkill1(), friend.getActiveSkill2()))
                                .passiveSkills(SkillBaseInfo.passiveSkill(friend.getPassiveSkill1(), friend.getPassiveSkill2()))
                                .build()
                ).toList();
        return new FriendListResponse(friendBaseInfos);
    }

    public static FriendListResponse empty() {
        return new FriendListResponse(List.of());
    }
}
