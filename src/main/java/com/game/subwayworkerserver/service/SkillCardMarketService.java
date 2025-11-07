package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.controller.response.GlobalSkillCardResponse;
import com.game.subwayworkerserver.controller.response.PaymentSuccessResponse;
import com.game.subwayworkerserver.controller.response.SkillOnSaleResponse;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import com.game.subwayworkerserver.entity.*;
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

    @Transactional
    public PaymentSuccessResponse getSkillCardSoldPayment(final String uuid, final long marketId) {
        UserEntity user = findUserByUuid(uuid);
        SkillCardMarketEntity skillCardMarketEntity =
                skillCardMarketRepository.findByIdAndSellerIdAndSoldTrueAndIsPaidFalseAndAvailableFalse(marketId, user.getId())
                        .orElseThrow(
                                () -> new BaseException(NOT_FOUND, "Coworker not found on market.")
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
        UserEntity user = findUserByUuid(uuid);
        SkillCardMarketEntity skillCardMarketEntity = skillCardMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "방금 판매된 스킬카드예요.")
        );
        checkUserVacationTicket(user, skillCardMarketEntity.getPrice());
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

    private static void checkUserVacationTicket(final UserEntity user, final int price) {
        boolean ableToBuy = user.hasEnoughVacationTickets(price);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "휴가티켓이 부족해요!");
        }
    }
}
