package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CoworkerFilterRequest;
import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.response.GlobalCoworkerResponse;
import com.game.subwayworkerserver.controller.response.MyCoworkersMarketResponse;
import com.game.subwayworkerserver.controller.response.PaymentSuccessResponse;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.CoworkerMarketEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.CoworkerMarketRepository;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CoworkerMarketService {

    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final CoworkerMarketRepository coworkerMarketRepository;
    private final CoworkerListFilter coworkerListFilter;

    public GlobalCoworkerResponse getCoworkerMarket(final String uuid, CoworkerFilterRequest request) {
        UserEntity user = findUserByUuid(uuid);
        List<CoworkerMarketEntity> marketCoworkers = coworkerMarketRepository.findAllByAvailableTrueAndSoldFalseAndSellerIdNot(user.getId());
        List<Long> coworkersId = getCoworkersId(marketCoworkers);

        List<CoworkerEntity> allByIdInAndOnSale = findCoworkersOnSaleByCoworkerIds(coworkersId);
        List<CoworkerOnSale> coworkerOnSalesDomainList = CoworkerOnSale.toDomain(marketCoworkers, allByIdInAndOnSale);
        List<CoworkerOnSale> filteredCoworkers = coworkerListFilter.filter(coworkerOnSalesDomainList, request);
        return GlobalCoworkerResponse.of(filteredCoworkers);
    }

    private List<CoworkerEntity> findCoworkersOnSaleByCoworkerIds(List<Long> coworkerIds) {
        return coworkerRepository.findAllByIdIn(coworkerIds);
    }

    private static List<Long> getCoworkersId(List<CoworkerMarketEntity> marketCoworkers) {
        return marketCoworkers.stream()
                .map(CoworkerMarketEntity::getCoworkerId)
                .toList();
    }

    private UserEntity findUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
    }

    public MyCoworkersMarketResponse getMyCoworkerOnSale(final String uuid) {
        UserEntity user = findUserByUuid(uuid);
        List<CoworkerMarketEntity> coworkersOnSale = coworkerMarketRepository.findAllBySellerIdOrderByIdDesc(user.getId());
        List<Long> coworkersId = getCoworkersId(coworkersOnSale);
        List<CoworkerEntity> coworkersOnSaleByCoworkerIds = findCoworkersOnSaleByCoworkerIds(coworkersId);
        List<CoworkerOnSale> coworkerOnSalesDomainList = CoworkerOnSale.toDomain(coworkersOnSale, coworkersOnSaleByCoworkerIds);
        return MyCoworkersMarketResponse.of(coworkerOnSalesDomainList);
    }

    @Transactional
    public void registerCoworkerOnMarket(final String uuid, final SellingRequest request) {
        UserEntity user = findUserByUuid(uuid);
        Long userId = user.getId();
        checkUserCoin(request.registrationFee(), user);

        CoworkerEntity coworkerEntity = coworkerRepository.findByUserIdAndId(userId, request.targetId()).orElseThrow(
                () -> new BaseException(NOT_FOUND, "CoworkerId not found.")
        );

        if (coworkerEntity.isInUse()) {
            throw new BaseException(BAD_REQUEST, "CoworkerId is already in use.");
        }
        boolean coworkerOnMarket = coworkerMarketRepository.existsByCoworkerIdAndSellerId(request.targetId(), userId);
        if (coworkerOnMarket) {
            throw new BaseException(BAD_REQUEST, "동료는 이미 판매중입니다.");
        }

        coworkerEntity.coworkerOnSale();
        CoworkerMarketEntity coworkerMarketEntity = CoworkerMarketEntity.of(userId, request);
        coworkerMarketRepository.save(coworkerMarketEntity);
        user.purchaseWithCoin(request.registrationFee());
    }

    private static void checkUserCoin(final int registrationFee, final UserEntity user) {
        boolean ableToBuy = user.hasCoinMoreThanItemPrice(registrationFee);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }
    }

    @Transactional
    public void cancelSellingMyCoworker(final String uuid, final long marketId) {
        UserEntity user = findUserByUuid(uuid);
        CoworkerMarketEntity coworkerMarketEntity = coworkerMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId).orElseThrow(
                () -> new BaseException(NOT_FOUND, "Coworker already sold.")
        );
        CoworkerEntity coworkerEntity = coworkerRepository.findByUserIdAndId(user.getId(), coworkerMarketEntity.getCoworkerId()).orElseThrow(
                () -> new BaseException(NOT_FOUND, "Coworker not found.")
        );
        coworkerEntity.cancelSelling();
        coworkerMarketRepository.delete(coworkerMarketEntity);
    }

    @Transactional
    public PaymentSuccessResponse getCoworkerSoldPayment(final String uuid, final long marketId) {
        UserEntity user = findUserByUuid(uuid);
        CoworkerMarketEntity coworkerMarketEntity =
                coworkerMarketRepository.findByIdAndSellerIdAndSoldTrueAndIsPaidFalseAndAvailableFalse(marketId, user.getId())
                        .orElseThrow(
                                () -> new BaseException(NOT_FOUND, "Coworker not found on market.")
                        );
        int coworkerRealPrice = coworkerMarketEntity.getPrice() - coworkerMarketEntity.getCommission();
        user.increaseVacationTicket(coworkerRealPrice);
        coworkerMarketEntity.paidDone();
        coworkerRepository.findByIdAndOnSaleTrue(coworkerMarketEntity.getCoworkerId()).ifPresent(CoworkerEntity::paidDone);
        return PaymentSuccessResponse.of(
                coworkerMarketEntity.getPrice(),
                coworkerMarketEntity.getCommission(),
                coworkerRealPrice
        );
    }

    @Transactional
    public void purchaseCoworker(final String uuid, final long marketId) {
        UserEntity user = findUserByUuid(uuid);
        CoworkerMarketEntity coworkerMarketEntity = coworkerMarketRepository.findByIdAndAvailableTrueAndSoldFalse(marketId)
                .orElseThrow(
                        () -> new BaseException(NOT_FOUND, "방금 판매된 동료에요.")
                );
        checkUserVacationTicket(user, coworkerMarketEntity.getPrice());
        coworkerMarketEntity.purchase(user.getId());

        coworkerRepository.findByIdAndOnSaleTrue(coworkerMarketEntity.getCoworkerId())
                .ifPresent(soldCoworker -> {
                    CoworkerEntity newOwner = CoworkerEntity.copyEntityForRegistration(soldCoworker);
                    CoworkerEntity saved = coworkerRepository.save(newOwner);
                    soldCoworker.registerNewUser(user.getId());
                    coworkerMarketEntity.changeCoworkerId(saved.getId());
                });

        int coworkerRealPrice = coworkerMarketEntity.getPrice() - coworkerMarketEntity.getCommission();
        user.purchaseWithVacationTickets(coworkerRealPrice);
    }

    private static void checkUserVacationTicket(final UserEntity user, final int price) {
        boolean ableToBuy = user.hasEnoughVacationTickets(price);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "휴가티켓이 부족해요!");
        }
    }
}
