package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.response.LuckyGachaEarnResponse;
import com.game.subwayworkerserver.controller.response.LuckyGachaListResponse;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.entity.CurrencyType;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.UserGachaRepository;
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
        return LuckyGachaEarnResponse.of(randomGacha, defaultReward, extraReward);
    }
}
