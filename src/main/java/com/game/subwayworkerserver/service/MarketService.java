package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.FriendFilterRequest;
import com.game.subwayworkerserver.controller.request.SellingRequest;
import com.game.subwayworkerserver.controller.response.GlobalFriendsResponse;
import com.game.subwayworkerserver.controller.response.MyFriendsMarketResponse;
import com.game.subwayworkerserver.domain.market.FriendOnSale;
import com.game.subwayworkerserver.entity.FriendEntity;
import com.game.subwayworkerserver.entity.FriendMarketEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.repository.FriendMarketRepository;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendMarketRepository friendMarketRepository;
    private final FriendListFilter friendListFilter;

    public GlobalFriendsResponse getFriendMarket(final String uuid, FriendFilterRequest request) {
        UserEntity user = findUserByUuid(uuid);
        List<FriendMarketEntity> marketFriends = friendMarketRepository.findAllByAvailableTrueAndSoldFalseAndSellerIdNot(user.getId());
        List<Long> friendIds = getFriendIds(marketFriends);

        List<FriendEntity> allByIdInAndOnSale = findFriendsOnSaleByFriendIds(friendIds);
        List<FriendOnSale> friendOnSalesDomainList = FriendOnSale.toDomain(marketFriends, allByIdInAndOnSale);
        List<FriendOnSale> filteredFriends = friendListFilter.filter(friendOnSalesDomainList, request);
        return GlobalFriendsResponse.of(filteredFriends);
    }

    private List<FriendEntity> findFriendsOnSaleByFriendIds(List<Long> friendIds) {
        return friendRepository.findAllByIdInAndOnSale(friendIds, true);
    }

    private static List<Long> getFriendIds(List<FriendMarketEntity> marketFriends) {
        return marketFriends.stream()
                .map(FriendMarketEntity::getFriendId)
                .toList();
    }

    private UserEntity findUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(USER_NOT_FOUND)
        );
    }

    public MyFriendsMarketResponse getMyFriendsOnSale(final String uuid) {
        UserEntity user = findUserByUuid(uuid);
        List<FriendMarketEntity> friendsOnSale = friendMarketRepository.findAllBySellerIdAndAvailableTrue(user.getId());
        List<Long> friendIds = getFriendIds(friendsOnSale);
        List<FriendEntity> friendsOnSaleByFriendIds = findFriendsOnSaleByFriendIds(friendIds);
        List<FriendOnSale> friendOnSalesDomainList = FriendOnSale.toDomain(friendsOnSale, friendsOnSaleByFriendIds);
        return MyFriendsMarketResponse.of(friendOnSalesDomainList);
    }

    @Transactional
    public void registerFriendOnMarket(final String uuid, final SellingRequest request) {
        UserEntity user = findUserByUuid(uuid);
        Long userId = user.getId();
        checkUserCoin(request.registrationFee(), user);

        FriendEntity friendEntity = friendRepository.findByUserIdAndId(userId, request.friendId()).orElseThrow(
                () -> new BaseException(NOT_FOUND, "Friend not found.")
        );

        if(friendEntity.isInUse()) {
            throw new BaseException(BAD_REQUEST, "Friend is already in use.");
        }

        friendEntity.friendOnSale();
        FriendMarketEntity friendMarketEntity = FriendMarketEntity.of(userId, request);
        friendMarketRepository.save(friendMarketEntity);
        user.purchaseWithCoin(request.registrationFee());
    }

    private static void checkUserCoin(final int registrationFee, final UserEntity user) {
        boolean ableToBuy = user.hasCoinMoreThanItemPrice(registrationFee);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "Not enough coin");
        }
    }
}
