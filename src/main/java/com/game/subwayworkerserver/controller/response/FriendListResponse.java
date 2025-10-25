package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
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
                                .activeSkills(ActiveSkillCard.of(friend.getActiveSkill1()) + "," + ActiveSkillCard.of(friend.getActiveSkill2()))
                                .passiveSkills(PassiveSkillCard.of(friend.getPassiveSkill1()) + "," + PassiveSkillCard.of(friend.getPassiveSkill2()))
                                .build()
                ).toList();
        return new FriendListResponse(friendBaseInfos);
    }
}
