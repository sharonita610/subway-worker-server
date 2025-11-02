package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.controller.request.SellingRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "friend_market")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendMarketEntity extends BaseTimeEntity {

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
    private long friendId;

    private long buyerId;

    @Column(nullable = false)
    private boolean sold = false;

    @Column(nullable = false)
    private boolean available = false;

    @Column(nullable = false)
    private boolean isPaid = false;

    private LocalDateTime soldTime;


    public FriendMarketEntity(
           long sellerId,  int price, int commission, CurrencyType currency, long friendId, boolean available
    ) {
        this.sellerId = sellerId;
        this.price = price;
        this.commission = commission;
        this.currency = currency;
        this.friendId = friendId;
        this.available = available;
    }


    public static FriendMarketEntity of(final long userId, final SellingRequest request) {
        return new FriendMarketEntity(
                userId,
                request.sellingPrice(),
                request.sellingCommission(),
                CurrencyType.VACATION_TICKET,
                request.friendId(),
                true
        );
    }
}
