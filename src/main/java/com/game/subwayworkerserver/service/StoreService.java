package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.StoreItemResponse;
import com.game.subwayworkerserver.domain.baseinfo.Coin;
import com.game.subwayworkerserver.domain.baseinfo.VacationTicket;
import com.game.subwayworkerserver.entity.ItemName;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserStoreMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;
    private final UserStoreMapRepository userStoreMapRepository;

    private static final String VACATION = "VACATION_TICKET";
    private static final String COIN = "COIN";
    private static final String TRAP = "TRAP";
    private static final String RANDOM_TRAP = "RANDOM_TRAP";
    private static final String CAPTURE = "CAPTURE";
    private static final String RANDOM_CAPTURE = "RANDOM_CAPTURE";

    public StoreItemResponse getStoreItems(final String uuid) {

        UserEntity user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
        Long userId = user.getId();

        boolean isFirst = userStoreMapRepository.existsByUserIdAndItemName(userId, VACATION);
        int todayRandomTrapPurchase = getTodayPurchaseCount(userId, RANDOM_TRAP);
        int todayRandomCapture = getTodayPurchaseCount(userId, RANDOM_CAPTURE);

        List<ItemName> trapList = ItemName.getTrapList();
        List<ItemName> captureList = ItemName.getCaptureList();
        List<Coin> coinList = Coin.getAllPacks();
        List<VacationTicket> vacationTicketList = VacationTicket.getAllPacks();

        return StoreItemResponse.of(
                trapList, todayRandomTrapPurchase,
                captureList,todayRandomCapture,
                coinList,
                vacationTicketList,
                !isFirst
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
}
