package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.market.FriendOnSale;

import java.util.List;

public record MyFriendsMarketResponse(
        List<MyFriedMarketInfo> friendList
) {
    public static MyFriendsMarketResponse of(List<FriendOnSale> friendOnSalesDomainList) {
        return new MyFriendsMarketResponse(
                friendOnSalesDomainList.stream()
                        .map(MyFriedMarketInfo::of)
                        .toList()
        );
    }

    private record MyFriedMarketInfo(
            int price,
            long marketId,
            boolean isPaid,
            boolean isSold,
            FriendBaseInfo friendBaseInfo
    ) {
        public static MyFriedMarketInfo of(FriendOnSale friendOnSale) {
            return new MyFriedMarketInfo(
                    friendOnSale.price(),
                    friendOnSale.marketId(),
                    friendOnSale.isPaid(),
                    friendOnSale.isSold(),
                    FriendBaseInfo.from(friendOnSale)
            );
        }
    }
}
