package com.game.subwayworkerserver.dto.response.market;

import com.game.subwayworkerserver.dto.response.coworker.CoworkerBaseInfo;
import com.game.subwayworkerserver.domain.market.CoworkerOnSale;

import java.util.List;

public record MyCoworkersMarketResponse(
        List<MyCoworkerMarketInfo> coworkerList
) {
    public static MyCoworkersMarketResponse of(List<CoworkerOnSale> coworkerOnSalesDomainList) {
        return new MyCoworkersMarketResponse(
                coworkerOnSalesDomainList.stream()
                        .map(MyCoworkerMarketInfo::of)
                        .toList()
        );
    }

    private record MyCoworkerMarketInfo(
            int price,
            long marketId,
            boolean isPaid,
            boolean isSold,
            CoworkerBaseInfo coworkerBaseInfo
    ) {
        public static MyCoworkerMarketInfo of(CoworkerOnSale coworkerOnSale) {
            return new MyCoworkerMarketInfo(
                    coworkerOnSale.price(),
                    coworkerOnSale.marketId(),
                    coworkerOnSale.isPaid(),
                    coworkerOnSale.isSold(),
                    CoworkerBaseInfo.fromDomain(coworkerOnSale)
            );
        }
    }
}
