package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "dungeon_record")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DungeonRecordEntity extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(nullable = false)
    private boolean isFirstClear;

    @Column(nullable = false)
    private boolean isRewardPaid;

    @Column(nullable = false)
    private LocalDateTime visitTime;

    @Column(nullable = false)
    private LocalDateTime retryTime;

    public void updateDungeonName(final String stationName) {
        this.stationName = stationName;
    }

    @Builder
    public DungeonRecordEntity(
            Long userId, String stationName, int successCount, int failCount, boolean isFirstClear, boolean isRewardPaid
    ) {
        this.userId = userId;
        this.stationName = stationName;
        this.successCount = successCount;
        this.failCount = failCount;
        this.isFirstClear = isFirstClear;
        this.isRewardPaid = isRewardPaid;
        this.visitTime = LocalDateTime.now();
        this.retryTime = visitTime.plusHours(8);
    }

    public void failDungeon(final String stationName) {
        this.stationName = stationName;
        this.failCount++;
    }

    public boolean canRetry() {
        return retryTime.isBefore(LocalDateTime.now());
    }

    public void updateSuccess() {
        this.successCount++;
        this.retryTime = LocalDateTime.now().plusHours(8);
    }
}
