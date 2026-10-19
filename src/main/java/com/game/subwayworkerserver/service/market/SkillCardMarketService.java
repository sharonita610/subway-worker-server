package com.game.subwayworkerserver.service.market;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.skill.SkillCardListFilter;
import com.game.subwayworkerserver.controller.request.market.SellingRequest;
import com.game.subwayworkerserver.controller.request.market.SkillCardFilterRequest;
import com.game.subwayworkerserver.controller.response.market.GlobalSkillCardResponse;
import com.game.subwayworkerserver.controller.response.market.PaymentSuccessResponse;
import com.game.subwayworkerserver.controller.response.market.SkillOnSaleResponse;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.SkillCardMarketRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SkillCardMarketService {

    private final UserFinder userFinder;
    private final SkillCardRepository skillCardRepository;
    private final SkillCardMarketRepository skillCardMarketRepository;
    private final SkillCardListFilter skillCardListFilter;

    public GlobalSkillCardResponse getSkillCardMarket(final String uuid, final SkillCardFilterRequest request) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<SkillCardMarketEntity> marketSkillCardLists = skillCardMarketRepository.findAllByAvailableTrueAndSoldFalseAndSellerIdNot(user.getId());
        List<Long> skillCardIds = getSkillCardIds(marketSkillCardLists);

        List<SkillCardEntity> skillCardOnSaleByCardIds = findSkillCardOnSaleByCardIds(skillCardIds);
        List<SkillCardOnSale> coworkerOnSalesDomainList = SkillCardOnSale.toDomain(marketSkillCardLists, skillCardOnSaleByCardIds);
        if(request == null) {
            return GlobalSkillCardResponse.of(coworkerOnSalesDomainList);
        }
        List<SkillCardOnSale> filteredCoworkers = skillCardListFilter.filter(coworkerOnSalesDomainList, request);
        return GlobalSkillCardResponse.of(filteredCoworkers);
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
        UserEntity user = userFinder.getByUuid(uuid);
        List<SkillCardMarketEntity> skillCardOnSale = skillCardMarketRepository.findAllBySellerIdOrderByIdDesc(user.getId());
        List<Long> skillCardIds = getSkillCardIds(skillCardOnSale);
        List<SkillCardEntity> skillCardEntityList = skillCardRepository.findAllByIdIn(skillCardIds);
        List<SkillCardOnSale> coworkerOnSalesDomainList = SkillCardOnSale.toDomain(skillCardOnSale, skillCardEntityList);
        return SkillOnSaleResponse.of(coworkerOnSalesDomainList);
    }

    @Transactional
    public void registerCardOnMarket(final String uuid, final SellingRequest request) {
        UserEntity user = userFinder.getByUuid(uuid);
        MarketValidator.checkUserCoin(request.registrationFee(), user);

        long userId = user.getId();
        long cardId = request.targetId();

        SkillCardEntity skillCard = getSkillCardRepositoryByUserIdAndId(userId, cardId);
        skillCard.sellOnMarket();
        verifySkillCardExistenceOnMarket(skillCard.getId(), userId);

        SkillCardMarketEntity skillCardMarketEntity = SkillCardMarketEntity.of(userId, request);
        skillCardMarketRepository.save(skillCardMarketEntity);

        user.purchaseWithCoin(request.registrationFee());
    }

    private void verifySkillCardExistenceOnMarket(final long skillCardId, long userId) {
        boolean exists = skillCardMarketRepository.existsBySkillCardIdAndSellerId(skillCardId, userId);
        if (exists) {
            throw new BaseException(BAD_REQUEST, "Skill card already on market");
        }
    }

    private SkillCardEntity getSkillCardRepositoryByUserIdAndId(final long userId, final long skillCardId) {
        return skillCardRepository.findByUserIdAndIdAndInUseFalse(userId, skillCardId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "Skill card not found or is already in use")
        );
    }

    @Transactional
    public void cancelSellingMySkillCard(final String uuid, final long marketId) {
        UserEntity user = userFinder.getByUuid(uuid);
        SkillCardMarketEntity skillCardMarket = skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "SkillCard already sold.")
        );
        SkillCardEntity skillCard = getSkillCardRepositoryByUserIdAndId(user.getId(), skillCardMarket.getSkillCardId());
        skillCard.cancelSelling();
        skillCardMarketRepository.delete(skillCardMarket);
    }

    @Transactional
    public PaymentSuccessResponse getSkillCardSoldPayment(final String uuid, final long marketId) {
        UserEntity user = userFinder.getByUuid(uuid);
        SkillCardMarketEntity skillCardMarketEntity =
                skillCardMarketRepository.findByIdAndSellerIdAndSoldTrueAndIsPaidFalseAndAvailableFalse(marketId, user.getId())
                        .orElseThrow(
                                () -> new BaseException(NOT_FOUND, "Skillcard not found on market.")
                        );

        int skillCardRealPrice = skillCardMarketEntity.getPrice() - skillCardMarketEntity.getCommission();
        user.increaseVacationTicket(skillCardRealPrice);
        skillCardMarketEntity.paidDone();

        skillCardRepository.findByIdAndOnSaleTrue(skillCardMarketEntity.getSkillCardId())
                .ifPresent(SkillCardEntity::paidDone);

        return PaymentSuccessResponse.of(
                skillCardMarketEntity.getPrice(),
                skillCardMarketEntity.getCommission(),
                skillCardRealPrice
        );
    }

    @Transactional
    public void purchaseSkillCard(final String uuid, final long marketId) {
        UserEntity user = userFinder.getByUuid(uuid);
        SkillCardMarketEntity skillCardMarketEntity = skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "방금 판매된 스킬카드예요.")
        );
        MarketValidator.checkUserVacationTicket(user, skillCardMarketEntity.getPrice());
        skillCardMarketEntity.purchase(user.getId());

        skillCardRepository.findByIdAndOnSaleTrue(skillCardMarketEntity.getSkillCardId())
                .ifPresent(soldCard -> {
                    SkillCardEntity newOwner = SkillCardEntity.copyEntityForRegistration(soldCard);
                    SkillCardEntity saved = skillCardRepository.save(newOwner);
                    soldCard.registerNewUser(user.getId());
                    skillCardMarketEntity.changeSkillCardId(saved.getId());
                });

        int skillCardRealPrice = skillCardMarketEntity.getPrice() - skillCardMarketEntity.getCommission();
        user.purchaseWithVacationTickets(skillCardRealPrice);
    }

}
