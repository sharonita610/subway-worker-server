package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.PurchaseRequest;
import com.game.subwayworkerserver.controller.response.StoreItemResponse;
import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserStoreMapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;
import static com.game.subwayworkerserver.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;
    private final UserStoreMapRepository userStoreMapRepository;
    private final UserItemMapRepository userItemMapRepository;

    private static final String VACATION = "VACATION_TICKET";
    private static final String COIN = "COIN";
    private static final String TRAP = "TRAP";
    private static final String RANDOM_TRAP = "RANDOM_TRAP";
    private static final String CAPTURE = "CAPTURE";
    private static final String RANDOM_CAPTURE = "RANDOM_CAPTURE";

    public StoreItemResponse getStoreItems(final String uuid) {

        Long userId = getUserByUuid(uuid).getId();

        boolean isFirst = userStoreMapRepository.existsByUserIdAndItemName(userId, VACATION);
        int todayRandomTrapPurchase = getTodayPurchaseCount(userId, RANDOM_TRAP);
        int todayRandomCapture = getTodayPurchaseCount(userId, RANDOM_CAPTURE);

        List<ItemName> trapList = ItemName.getTrapList();
        List<ItemName> captureList = ItemName.getCaptureList();
        List<Coin> coinList = Coin.getAllPacks();
        List<VacationTicket> vacationTicketList = VacationTicket.getAllPacks();

        return StoreItemResponse.of(
                trapList, todayRandomTrapPurchase,
                captureList, todayRandomCapture,
                coinList,
                vacationTicketList,
                !isFirst
        );
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
    }

    public int getTodayPurchaseCount(Long userId, String itemName) {
        // 1. 오늘의 시작 시각 (00:00:00) 계산
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 2. 오늘의 종료 시각 (23:59:59.999...) 계산
        LocalDateTime endOfToday = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);

        // 3. Repository 호출
        long count = userStoreMapRepository.countByUserIdAndItemNameAndPurchaseDateBetween(
                userId,
                itemName,
                startOfToday,
                endOfToday
        );
        return (int) count;
    }

    @Transactional
    public void purchaseItemWithCoin(String uuid, PurchaseRequest request) {
        UserEntity user = getUserByUuid(uuid);
        String name = request.name();

        if(name.contains(RANDOM_TRAP) || name.contains(RANDOM_CAPTURE)) {
            throw new BaseException(BAD_REQUEST, "Cannot purchase random item with this api.");
        }

        ItemName itemName = ItemName.fromConstantName(name);
        int coinNeeded = itemName.getPrice();

        boolean ableToBuy = user.hasCoinMoreThanItemPrice(coinNeeded);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }

        user.purchaseWithCoin(coinNeeded);
        saveUserItemIntoMap(user.getId(), itemName);
        saveUserStoreRecord(user.getId(), String.valueOf(itemName),1, coinNeeded, CurrencyType.COIN);
    }

    private void saveUserStoreRecord(long userId, String itemName, int quantity, int price , CurrencyType currencyType) {
        UserStoreMapEntity userStoreMapEntity = UserStoreMapEntity.of(
                userId, itemName, quantity, price, currencyType
        );
        userStoreMapRepository.save(userStoreMapEntity);
    }

    private void saveUserItemIntoMap(long userId, ItemName itemName) {
        userItemMapRepository.findByUserIdAndItemName(userId, itemName)
                .ifPresentOrElse(
                        UserItemMapEntity::increaseQuantity,
                        () -> {
                            UserItemMapEntity userItemMapEntity = UserItemMapEntity.of(userId, itemName);
                            userItemMapRepository.save(userItemMapEntity);
                        }
                );
    }

    @Transactional
    public void purchaseCoinWithVacationTicket(String uuid, PurchaseRequest request) {
        UserEntity user = getUserByUuid(uuid);
        String name = request.name();
        Coin coin = Coin.valueOf(request.name());
        int vacationTicketNeeded = coin.getTicketPrice();

        boolean ableToBuy = user.hasEnoughVacationTickets(vacationTicketNeeded);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }

        user.purchaseWithVacationTickets(coin.getAcquiredCoin(), vacationTicketNeeded);
        saveUserStoreRecord(user.getId(), COIN, coin.getAcquiredCoin(), vacationTicketNeeded, CurrencyType.VACATION_TICKET);

    }

    @Transactional
    public void buyVacationTicket(String uuid, PurchaseRequest request) {
        UserEntity user = getUserByUuid(uuid);
        VacationTicket vacationTicket = VacationTicket.valueOf(request.name());

        // 외부 결제 api

        user.purchaseVacationTicket(vacationTicket.getAcquiredTickets());
        saveUserStoreRecord(
                user.getId(),
                VACATION,
                vacationTicket.getAcquiredTickets(),
                vacationTicket.getPriceKrw(),
                CurrencyType.CASH
        );

    }


    @Transactional
    public void purchaseRandomItem(String uuid, PurchaseRequest request) {
        UserEntity user = getUserByUuid(uuid);
        String name = request.name();
        ItemName itemName = ItemName.getRandomItem(name);

        // 광고보고 오기

        saveUserItemIntoMap(user.getId(), itemName);
        saveUserStoreRecord(user.getId(), name, 1, 0, CurrencyType.FREE);
    }
}

