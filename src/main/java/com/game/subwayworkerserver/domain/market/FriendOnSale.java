package com.game.subwayworkerserver.domain.market;

import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.entity.FriendEntity;
import com.game.subwayworkerserver.entity.FriendMarketEntity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record FriendOnSale(
        long marketId,
        long friendId,
        String engName,
        String korName,
        boolean inUse,
        boolean onSale,
        int level,
        String position,
        String attribute,
        long hp,
        long attack,
        long defense,
        long mana,
        long maxMana,
        long manaRecovery,
        int activeSkillId1,
        int activeSkillId2,
        int passiveSkillId1,
        int passiveSkillId2,
        List<ActiveSkillCard> activeSkills,
        List<PassiveSkillCard> passiveSkills,
        int price,
        long sellerId,
        boolean isPaid,
        boolean isSold
) {
    // 2. 정적 팩토리 메서드 구현
    public static List<FriendOnSale> toDomain(final List<FriendMarketEntity> marketFriends, final List<FriendEntity> allFriendsOnSale) {

        // 1. FriendEntity 목록을 Map<FriendId, FriendEntity>으로 변환하여 빠른 조회를 준비합니다.
        Map<Long, FriendEntity> friendMap = allFriendsOnSale.stream()
                .collect(Collectors.toMap(
                        FriendEntity::getId, // Key: FriendEntity의 ID (Long)
                        friend -> friend     // Value: FriendEntity 객체 자체
                ));

        // 2. FriendMarketEntity 목록을 순회하며 데이터 결합 및 DTO로 변환합니다.
        return marketFriends.stream()
                .map(market -> {
                    Long friendId = market.getFriendId(); // FriendMarketEntity에서 friendId 추출
                    FriendEntity friend = friendMap.get(friendId); // Map에서 상세 FriendEntity 조회

                    // 데이터가 누락되었거나 필터링 조건에 의해 제거된 경우 (null일 경우) 스킵
                    if (friend == null) {
                        return null;
                    }

                    // 3. Builder를 사용하여 FriendOnSale 객체 생성
                    return FriendOnSale.builder()
                            // FriendMarketEntity (마켓 정보)
                            .marketId(market.getId())
                            .friendId(friendId)
                            .price(market.getPrice())
                            .sellerId(market.getSellerId())
                            .isPaid(market.isPaid())
                            .isSold(market.isSold())
                            // FriendEntity (상세 스탯 정보)
                            .engName(friend.getFriendType().name())
                            .korName(friend.getName())
                            .level(friend.getLevel().getLevel())
                            .position(friend.getLevel().getTitle())
                            .inUse(friend.isInUse())
                            .onSale(friend.isOnSale())
                            .attribute(friend.getFriendAttribute().getKoreanName())
                            .hp(friend.getHp())
                            .attack(friend.getAttack())
                            .defense(friend.getDefense())
                            .mana(friend.getMana())
                            .maxMana(friend.getMaxMana())
                            .manaRecovery(friend.getManaRecovery())
                            .activeSkillId1(friend.getActiveSkill1())
                            .activeSkillId2(friend.getActiveSkill2())
                            .passiveSkillId1(friend.getPassiveSkill1())
                            .passiveSkillId2(friend.getPassiveSkill2())
                            .activeSkills(friend.getActiveSkills()) // FriendEntity에 ActiveSkills가 있다고 가정
                            .passiveSkills(friend.getPassiveSkills()) // FriendEntity에 PassiveSkills가 있다고 가정
                            .build();
                })
                .filter(java.util.Objects::nonNull) // 매칭 실패(null) 항목 제거
                .collect(Collectors.toList());
    }
}