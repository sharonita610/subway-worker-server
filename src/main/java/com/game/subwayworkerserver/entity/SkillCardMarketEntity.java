package com.game.subwayworkerserver.entity;

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
}
