package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

import java.util.List;

@Getter
public enum Coin {
    // (획득 코인, 티켓 가격, 설명)

    COIN_PACK_10K(10000, 10, "휴가 티켓 10개로 10,000 코인 구매"),

    COIN_PACK_60K(60000, 50, "휴가 티켓 50개로 60,000 코인 구매"),

    COIN_PACK_140K(140000, 100, "휴가 티켓 100개로 140,000 코인 구매");

    Coin(int acquiredCoin, int ticketPrice, String description) {
        this.acquiredCoin = acquiredCoin;
        this.ticketPrice = ticketPrice;
        this.description = description;
    }

    private final int acquiredCoin;   // 획득 코인 양 (상단 박스)
    private final int ticketPrice;    // 바캉스 티켓 가격 (하단 박스)
    private final String description; // 설명

    public static Coin fromTicketPrice(int ticketPrice) {
        for (Coin coinPack : values()) {
            if (coinPack.ticketPrice == ticketPrice) {
                return coinPack;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 티켓 가격입니다: " + ticketPrice);
    }

    public static List<Coin> getAllPacks() {
        return List.of(Coin.values());
    }
}
