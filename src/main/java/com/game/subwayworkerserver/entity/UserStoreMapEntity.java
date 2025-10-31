package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_store_map")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStoreMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private int price;

    // 5. 사용된 재화 (티켓, 코인, 원화 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false)
    private CurrencyType currencyType;

    // 6. 구매 날짜
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Builder
    public UserStoreMapEntity(Long userId, String itemName, int quantity, int price, CurrencyType currencyType) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.currencyType = currencyType;
        this.purchaseDate = LocalDateTime.now();
    }

    public static UserStoreMapEntity of(Long userId, String itemName, int quantity, int price, CurrencyType currencyType) {
        return UserStoreMapEntity.builder()
                .userId(userId)
                .itemName(itemName)
                .quantity(quantity)
                .price(price)
                .currencyType(currencyType)
                .build();
    }
}