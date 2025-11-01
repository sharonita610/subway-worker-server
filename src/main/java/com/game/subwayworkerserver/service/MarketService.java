package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.GlobalFriendsResponse;
import com.game.subwayworkerserver.entity.FriendMarketEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.FriendMarketRepository;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendMarketRepository friendMarketRepository;

    public GlobalFriendsResponse getFriendsOnSale(final String uuid) {
        findUserByUuid(uuid);
        List<FriendMarketEntity> allFriends = friendMarketRepository.findAllByAvailableTrueAndSoldFalse();


        return null;
    }

    private UserEntity findUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
    }
}
