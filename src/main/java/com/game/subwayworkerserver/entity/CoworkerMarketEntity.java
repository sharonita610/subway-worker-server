package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.CurrencyType;
import com.game.subwayworkerserver.dto.request.market.SellingRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coworker_market")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoworkerMarketEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private int price;

    @Column(length = 50, nullable = false)
    private int commission;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(nullable = false)
    private long sellerId;

    @Column(nullable = false)
    private long coworkerId;

    private long buyerId;

    @Column(nullable = false)
    private boolean sold = false;

    @Column(nullable = false)
    private boolean available = false;

    @Column(nullable = false)
    private boolean isPaid = false;

    private LocalDateTime soldTime;


    public CoworkerMarketEntity(
            long sellerId, int price, int commission, CurrencyType currency, long coworkerId, boolean available
    ) {
        this.sellerId = sellerId;
        this.price = price;
        this.commission = commission;
        this.currency = currency;
        this.coworkerId = coworkerId;
        this.available = available;
    }


    public static CoworkerMarketEntity of(final long userId, final SellingRequest request) {
        return new CoworkerMarketEntity(
                userId,
                request.sellingPrice(),
                request.sellingCommission(),
                CurrencyType.VACATION_TICKET,
                request.targetId(),
                true
        );
    }

    public void paidDone() {
        this.isPaid = true;
    }

    public void purchase(final long buyerId) {
        this.buyerId = buyerId;
        this.soldTime = LocalDateTime.now();
        this.sold = true;
        this.available = false;
    }

    public void changeCoworkerId(final long newCoworkerId) {
        this.coworkerId = newCoworkerId;
    }
}
