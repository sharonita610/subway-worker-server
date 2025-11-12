package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "stage_record")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageRecordEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int stageLevel;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(nullable = false)
    private boolean isFirstClear;

    @Column(nullable = false)
    private boolean isRewardPaid;

    public void updateStageLevel(final int requestedLevel) {
        this.stageLevel = requestedLevel;
    }

    @Builder
    public StageRecordEntity(Long userId, int stageLevel, int successCount, int failCount, boolean isFirstClear, boolean isRewardPaid) {
        this.userId = userId;
        this.stageLevel = stageLevel;
        this.successCount = successCount;
        this.failCount = failCount;
        this.isFirstClear = isFirstClear;
        this.isRewardPaid = isRewardPaid;
    }
}
