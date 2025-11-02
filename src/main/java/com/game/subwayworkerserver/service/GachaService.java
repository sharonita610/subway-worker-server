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
import org.springframework.stereotype.Service;

import java.util.List;

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

        List<GachaTreasure> randomGacha = GachaTreasure.getRandom();
        List<DefaultGift> defaultReward = GachaRandomGiftGenerator.getDefaultReward();
        List<ExtraGift> extraReward = GachaRandomGiftGenerator.getExtraReward();

        user.purchaseWithTickets(currencyType.getCurrencyType(), request.price());
        processGachaPurchase(user.getId(), randomGacha);
        processDefaultGift(defaultReward, user);
        // Service Layer 메서드 내부라고 가정합니다.
        processExtraGift(extraReward, user.getId());

        return LuckyGachaEarnResponse.of(randomGacha, defaultReward, extraReward);
    }

    private void processExtraGift(List<ExtraGift> extraReward, long userId) {
        extraReward.forEach(
                gift -> {
                    if (gift.type().equals("EXTRA_FRIEND")) {
                        UserGachaEntity userGachaEntity = UserGachaEntity.of(userId, GachaTreasure.valueOf(gift.type()));
                        gachaRepository.save(userGachaEntity);
                    }

                    ItemName acquiredItem = ItemName.valueOf(gift.type().toUpperCase());
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
                }
        );
    }

    private static void processDefaultGift(List<DefaultGift> defaultReward, UserEntity user) {
        defaultReward.forEach(
                gift -> {
                    if(gift.type().equals("coin")) {
                        user.increaseCoin(gift.quantity());
                    } else if(gift.type().equals("vacationTicket")) {
                        user.increaseVacationTicket(gift.quantity());
                    }
                }
        );
    }


    public void processGachaPurchase(long userId, List<GachaTreasure> randomGacha) {
        randomGacha.forEach(treasure -> {
            UserGachaEntity existingRecord = gachaRepository.findByUserIdAndGacha(userId, treasure).get();
            existingRecord.increaseGachaCount();
        });
    }
}
