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
    private Long userId;

    private long mailId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardSource source;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean isClaimed = false;

    @Builder
    public RewardEntity(Long userId, long mailId, RewardType type, RewardSource source, int quantity, boolean isClaimed) {
        this.userId = userId;
        this.mailId = mailId;
        this.type = type;
        this.source = source;
        this.quantity = quantity;
        this.isClaimed = isClaimed;
    }
}