package com.game.subwayworkerserver.domain.baseinfo;

import lombok.Getter;

import java.util.List;

@Getter
public enum VacationTicket {

    // --- 첫 구매 혜택 상품 ---
    // 첫 구매 상품은 첫 번째 4900원 상품에 해당한다고 가정
    FIRST_BUY_PACK(500, 4950, true, "첫 구매 한정! 500개 획득"),

    // --- 일반 상품 ---
    PACK_300(300, 4950, false, "휴가 티켓 300개"),
    PACK_700(700, 9900, false, "휴가 티켓 700개"),
    PACK_1600(1600, 19800, false, "휴가 티켓 1,600개");

    private final int acquiredTickets; // 획득 티켓 수
    private final int priceKrw;        // KRW 가격
    private final boolean isFirstBuyOffer; // 첫 구매 혜택 여부
    private final String description;    // 상품 설명

    VacationTicket(int acquiredTickets, int priceKrw, boolean isFirstBuyOffer, String description) {
        this.acquiredTickets = acquiredTickets;
        this.priceKrw = priceKrw;
        this.isFirstBuyOffer = isFirstBuyOffer;
        this.description = description;
    }

    /**
     * 가격과 획득 수량으로 상품을 찾는 정적 헬퍼 메서드.
     * (동일한 가격/수량이 있을 경우 isFirstBuyOffer를 기준으로 구분합니다.)
     * * @param acquiredTickets 획득 티켓 수
     * @param priceKrw 상품 가격 (KRW)
     * @return 해당하는 VacationTicket 상품 Enum 상수
     * @throws IllegalArgumentException 유효하지 않은 상품 조합일 경우
     */
    public static VacationTicket findPack(int acquiredTickets, int priceKrw) {
        for (VacationTicket pack : values()) {
            if (pack.acquiredTickets == acquiredTickets && pack.priceKrw == priceKrw) {
                return pack;
            }
        }
        throw new IllegalArgumentException(
                "유효하지 않은 휴가 티켓 상품 조합입니다: " + acquiredTickets + "개, " + priceKrw + "원");
    }

    public static List<VacationTicket> getAllPacks() {
        return List.of(VacationTicket.values());
    }
}