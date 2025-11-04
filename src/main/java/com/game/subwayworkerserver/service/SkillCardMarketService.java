package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.controller.response.GlobalSkillCardResponse;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.SkillCardMarketRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SkillCardMarketService {

    private final UserRepository userRepository;
    private final SkillCardRepository skillCardRepository;
    private final SkillCardMarketRepository skillCardMarketRepository;
    private final SkillCardListFilter skillCardListFilter;

    public GlobalSkillCardResponse getSkillCardMarket(final String uuid, final SkillCardFilterRequest request) {
        UserEntity user = findUserByUuid(uuid);
        List<SkillCardMarketEntity> marketSkillCardLists = skillCardMarketRepository.findAllByAvailableTrueAndSoldFalseAndSellerIdNot(user.getId());
        List<Long> skillCardIds = getSkillCardIds(marketSkillCardLists);

        List<SkillCardEntity> skillCardOnSaleByCardIds = findSkillCardOnSaleByCardIds(skillCardIds);
        List<SkillCardOnSale> friendOnSalesDomainList = SkillCardOnSale.toDomain(marketSkillCardLists, skillCardOnSaleByCardIds);
        List<SkillCardOnSale> filteredFriends = skillCardListFilter.filter(friendOnSalesDomainList, request);
        return GlobalSkillCardResponse.of(filteredFriends);
    }

    private UserEntity findUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
    }

    private List<SkillCardEntity> findSkillCardOnSaleByCardIds(final List<Long> skillCardIds) {
        return skillCardRepository.findAllByIdInAndOnSaleTrue(skillCardIds);
    }

    private static List<Long> getSkillCardIds(final List<SkillCardMarketEntity> marketSkillCardLists) {
        return marketSkillCardLists.stream()
                .map(SkillCardMarketEntity::getSkillCardId)
                .toList();
    }
}
