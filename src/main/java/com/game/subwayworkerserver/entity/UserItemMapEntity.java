package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_item_map")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserItemMapEntity extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ItemName itemName;

    @Column(nullable = false)
    private int quantity;


    public UserItemMapEntity(Long userId, ItemName itemName, int quantity) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public static UserItemMapEntity of(final long userId, final ItemName itemName) {
       return new UserItemMapEntity(userId, itemName, 1);
    }

    public static UserItemMapEntity of(long userId, ItemName itemName, int quantityToAdd) {
        return new UserItemMapEntity(userId, itemName, quantityToAdd);
    }

    public void increaseQuantity() {
        this.quantity += 1;
    }

    public void decreaseQuantity() {
        if (this.quantity <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Cannot decrease quantity: Item quantity is already zero or below.");
        }
        this.quantity -= 1;
    }

    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }
}
