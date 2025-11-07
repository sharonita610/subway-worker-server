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
@Table(name = "skill_card_market")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillCardMarketEntity extends BaseTimeEntity {
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
    private long skillCardId;

    private long buyerId;

    @Column(nullable = false)
    private boolean sold = false;

    @Column(nullable = false)
    private boolean available = false;

    @Column(nullable = false)
    private boolean isPaid = false;

    private LocalDateTime soldTime;

    public SkillCardMarketEntity(
            long sellerId, int price, int commission, CurrencyType currency, long skillCardId, boolean available
    ) {
        this.sellerId = sellerId;
        this.price = price;
        this.commission = commission;
        this.currency = currency;
        this.skillCardId = skillCardId;
        this.available = available;
    }

    public static SkillCardMarketEntity of(final long userId, final SellingRequest request) {
        return new SkillCardMarketEntity(
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
}
