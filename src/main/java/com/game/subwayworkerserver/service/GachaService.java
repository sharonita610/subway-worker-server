package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.response.LuckyGachaEarnResponse;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerLevel;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.baseinfo.GachaPaymentOption;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserGachaRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.DefaultGift;
import com.game.subwayworkerserver.service.GachaRandomGiftGenerator.ExtraGift;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GachaService {

    private final UserRepository userRepository;
    private final UserGachaRepository gachaRepository;
    private final CoworkerRepository coworkerRepository;
    private final UserItemMapRepository userItemMapRepository;

    private static final String MATERIAL_COWORKER = "MATERIAL_COWORKER";

    public LuckyGachaListResponse getGachaList(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<UserGachaEntity> gachaEntityList = gachaRepository.findAllByUserId(user.getId());
        return LuckyGachaListResponse.of(user, gachaEntityList);
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional
    public LuckyGachaEarnResponse openLuckyGacha(final String uuid, final OpenLuckyGachaRequest request) {
        UserEntity user = getUserByUuid(uuid);
        CurrencyType currencyType = CurrencyType.confirm(request.payCondition());
        GachaPaymentOption gachaPaymentOption = GachaPaymentOption.confirmCountAndPrice(currencyType, request.price());

        checkUserHasEnoughTicketsToPay(request, user, currencyType);
        Map<GachaTreasure, Integer> userTreasureCounts = getUserTreasureCounts(user.getId());

        List<DefaultGift> defaultReward = GachaRandomGiftGenerator.getDefaultReward(gachaPaymentOption.getCount());
        List<ExtraGift> extraReward = GachaRandomGiftGenerator.getExtraReward(gachaPaymentOption.getCount(), userTreasureCounts);

        user.purchaseWithTickets(currencyType.getCurrencyType(), request.price());

        processExtraGift(extraReward, user.getId());
        processDefaultGift(defaultReward, user);

        return LuckyGachaEarnResponse.of(defaultReward, extraReward);
    }

    private Map<GachaTreasure, Integer> getUserTreasureCounts(final long userId) {
        List<UserGachaEntity> userGachaRecords = gachaRepository.findAllByUserId(userId);
        return userGachaRecords.stream()
                .collect(Collectors.toMap(
                        UserGachaEntity::getGacha,
                        UserGachaEntity::getGachaCount
                ));
    }

    private void checkUserHasEnoughTicketsToPay(
            final OpenLuckyGachaRequest request, final UserEntity user, final CurrencyType currencyType
    ) {
        boolean enoughTickets = user.hasEnoughTickets(currencyType.getCurrencyType(), request.price());
        if (!enoughTickets) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Not enough tickets.");
        }
    }

    private void processExtraGift(final List<ExtraGift> extraReward, final long userId) {
        extraReward.forEach(
                gift -> {
                    String giftType = gift.type();

                    if (giftType.equals(MATERIAL_COWORKER)) {
                        processExtraCoworkerReward(userId);
                    } else if (GachaTreasure.isGacha(giftType)) {
                        processGachaTreasureReward(userId, giftType);
                    } else {
                        processItemReward(userId, gift, giftType);
                    }
                }
        );
    }

    private void processItemReward(final long userId, final ExtraGift gift, final String giftType) {
        try {
            ItemName acquiredItem = ItemName.valueOf(giftType);
            userItemMapRepository.findByUserIdAndItemName(userId, acquiredItem)
                    .ifPresentOrElse(
                            existingItem -> {
                                existingItem.increaseQuantity(gift.quantity());
                            },
                            () -> {
                                UserItemMapEntity newUserItem = UserItemMapEntity.of(
                                        userId,
                                        acquiredItem
                                );
                                userItemMapRepository.save(newUserItem);
                            }
                    );
            log.info("User {} acquired item {} x{}.", userId, acquiredItem.name(), gift.quantity());

        } catch (IllegalArgumentException e) {
            log.error("Unknown Item Type received: {}", giftType);
            throw new BaseException(ErrorCode.BAD_REQUEST, "알 수 없는 보상 아이템 타입: " + giftType);
        }
    }

    private void processGachaTreasureReward(final long userId, final String giftType) {
        GachaTreasure treasure = GachaTreasure.fromItemName(giftType);

        gachaRepository.findByUserIdAndGacha(userId, treasure)
                .ifPresentOrElse(
                        UserGachaEntity::increaseGachaCount,
                        () -> {
                            UserGachaEntity newRecord = UserGachaEntity.of(userId, treasure); // from 메서드 수정 필요
                            gachaRepository.save(newRecord);
                        }
                );
        log.info("User {} Gacha Treasure {} count increased.", userId, treasure.getItemName());
    }

    private void processExtraCoworkerReward(final long userId) {
        CoworkerEntity coworkerEntity = CoworkerEntity.of(userId, CoworkerType.getRandomAttribute(), CoworkerLevel.getSpecialCoworkerLevel());
        coworkerRepository.save(coworkerEntity);
        log.info("User {} received MATERIAL_COWORKER reward.", userId);
    }

    private void processDefaultGift(final List<DefaultGift> defaultReward, final UserEntity user) {
        defaultReward.forEach(
                gift -> {
                    if (gift.type().equals("coin")) {
                        user.increaseCoin(gift.quantity());
                    } else if (gift.type().equals("vacationTicket")) {
                        user.increaseVacationTicket(gift.quantity());
                    }
                }
        );
    }
}
