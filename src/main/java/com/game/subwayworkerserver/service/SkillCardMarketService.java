package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.controller.response.GlobalSkillCardResponse;
import com.game.subwayworkerserver.controller.response.SkillOnSaleResponse;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.SkillCardMarketEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.SkillCardMarketRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.*;

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
        List<SkillCardOnSale> coworkerOnSalesDomainList = SkillCardOnSale.toDomain(marketSkillCardLists, skillCardOnSaleByCardIds);
        List<SkillCardOnSale> filteredCoworkers = skillCardListFilter.filter(coworkerOnSalesDomainList, request);
        return GlobalSkillCardResponse.of(filteredCoworkers);
    }

    private UserEntity findUserByUuid(final String uuid) {
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

    public SkillOnSaleResponse getMySkillCardOnSale(final String uuid) {
        UserEntity user = findUserByUuid(uuid);
        List<SkillCardMarketEntity> skillCardOnSale = skillCardMarketRepository.findAllBySellerId(user.getId());
        List<Long> skillCardIds = getSkillCardIds(skillCardOnSale);
        List<SkillCardEntity> skillCardEntityList = skillCardRepository.findAllByIdInAndOnSaleTrue(skillCardIds);
        List<SkillCardOnSale> coworkerOnSalesDomainList = SkillCardOnSale.toDomain(skillCardOnSale, skillCardEntityList);
        return SkillOnSaleResponse.of(coworkerOnSalesDomainList);
    }

    @Transactional
    public void registerCardOnMarket(final String uuid, final SellingRequest request) {
        UserEntity user = findUserByUuid(uuid);
        checkUserCoin(request.registrationFee(), user);

        long userId = user.getId();
        long cardId = request.targetId();

        SkillCardEntity skillCard = getSkillCardRepositoryByUserIdAndId(userId, cardId);
        verifySkillCardExistenceOnMarket(skillCard.getId(), cardId);

        SkillCardMarketEntity skillCardMarketEntity = SkillCardMarketEntity.of(userId, request);
        skillCardMarketRepository.save(skillCardMarketEntity);

        user.purchaseWithCoin(request.registrationFee());
    }

    private void verifySkillCardExistenceOnMarket(final long skillCardId, long cardId) {
        boolean exists = skillCardMarketRepository.existsBySkillCardIdAndSellerId(skillCardId, cardId);
        if (exists) {
            throw new BaseException(BAD_REQUEST, "Skill card already on market");
        }
    }

    private SkillCardEntity getSkillCardRepositoryByUserIdAndId(final long userId, final long skillCardId) {
        return skillCardRepository.findByUserIdAndIdAndInUseFalse(userId, skillCardId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "Skill card not found or is already in use")
        );
    }

    private static void checkUserCoin(final int registrationFee, final UserEntity user) {
        boolean ableToBuy = user.hasCoinMoreThanItemPrice(registrationFee);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }
    }

    @Transactional
    public void cancelSellingMySkillCard(final String uuid, final long marketId) {
        UserEntity user = findUserByUuid(uuid);
        SkillCardMarketEntity skillCardMarket = skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "SkillCard already sold.")
        );
        SkillCardEntity skillCard = getSkillCardRepositoryByUserIdAndId(user.getId(), skillCardMarket.getSkillCardId());
        skillCard.cancelSelling();
        skillCardMarketRepository.delete(skillCardMarket);
    }
}
