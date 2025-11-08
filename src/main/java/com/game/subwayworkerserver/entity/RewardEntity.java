package com.game.subwayworkerserver.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@Entity
@Table(name = "reward")
@NoArgsConstructor
public class RewardEntity extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mailId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType type;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public RewardEntity(long mailId, RewardType type, int quantity) {
        this.mailId = mailId;
        this.type = type;
        this.quantity = quantity;
    }
}