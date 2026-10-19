package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.reward.RewardType;
import com.game.subwayworkerserver.domain.reward.RewardSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@Entity
@Table(name = "reward")
@NoArgsConstructor
public class RewardEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private long mailId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private RewardType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private RewardSource source;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false)
    private boolean isClaimed = false;

    private String extraInfo;

    @Builder
    public RewardEntity(
            Long userId, long mailId, RewardType type, RewardSource source, long quantity, boolean isClaimed,
            String extraInfo
    ) {
        this.userId = userId;
        this.mailId = mailId;
        this.type = type;
        this.source = source;
        this.quantity = quantity;
        this.isClaimed = isClaimed;
        this.extraInfo = extraInfo;
    }

    public void claimRewardDone() {
        this.isClaimed = true;
    }
}
