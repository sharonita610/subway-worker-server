package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.response.LuckyGachaEarnResponse;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GachaService {

    private final UserRepository userRepository;
    private final UserGachaRepository gachaRepository;
    private final UserItemMapRepository userItemMapRepository;

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
        boolean enoughTickets = user.hasEnoughTickets(currencyType.getCurrencyType(), request.price());
        if (!enoughTickets) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Not enough tickets");
        }

        List<DefaultGift> defaultReward = GachaRandomGiftGenerator.getDefaultReward();
        List<ExtraGift> extraReward = GachaRandomGiftGenerator.getExtraReward();

        user.purchaseWithTickets(currencyType.getCurrencyType(), request.price());
        processExtraGift(extraReward, user.getId());
        processDefaultGift(defaultReward, user);

        return LuckyGachaEarnResponse.of(defaultReward, extraReward);
    }

    private static final String EXTRA_FRIEND_TYPE = "EXTRA_FRIEND";

    private void processExtraGift(List<ExtraGift> extraReward, long userId) {
        extraReward.forEach(
                gift -> {
                    String giftType = gift.type();

                    if (giftType.equals(EXTRA_FRIEND_TYPE)) {

                        log.info("User {} received EXTRA_FRIEND reward.", userId);
                    }

                    // --- 2. GachaTreasure (보물 아이템) 처리 ---

                    // GachaTreasure Enum에 해당 아이템 이름이 속하는지 확인
                    else if (GachaTreasure.isGacha(giftType)) {
                        System.out.println("giftType = " + giftType);
                        GachaTreasure treasure = GachaTreasure.fromItemName(giftType);

                        // GachaTreasure 기록 횟수 증가 (이미 존재한다고 가정하거나 FindOrCreate 로직 사용)
                        gachaRepository.findByUserIdAndGacha(userId, treasure)
                                .ifPresentOrElse(
                                        UserGachaEntity::increaseGachaCount,
                                        () -> {
                                            // 존재하지 않으면 새로 생성 (gachaCount 1로 초기화)
                                            UserGachaEntity newRecord = UserGachaEntity.of(userId, treasure); // from 메서드 수정 필요
                                            gachaRepository.save(newRecord);
                                        }
                                );
                        log.info("User {} Gacha Treasure {} count increased.", userId, treasure.getItemName());
                    }

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
                        // ItemName Enum에 없는 타입이 들어왔을 경우
                        log.error("Unknown Item Type received: {}", giftType);
                        throw new BaseException(ErrorCode.BAD_REQUEST, "알 수 없는 보상 아이템 타입: " + giftType);
                    }
                }
        );
    }
    private static void processDefaultGift(List<DefaultGift> defaultReward, UserEntity user) {
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
