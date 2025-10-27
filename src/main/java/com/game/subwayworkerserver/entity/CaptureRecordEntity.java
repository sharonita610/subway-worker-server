package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "capture_record")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaptureRecordEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(nullable = false)
    private int totalTryCount;

    public CaptureRecordEntity(Long userId, int successCount, int failCount, int totalTryCount) {
        this.userId = userId;
        this.successCount = successCount;
        this.failCount = failCount;
        this.totalTryCount = totalTryCount;
    }

    public static CaptureRecordEntity of(final long userId) {
        return new CaptureRecordEntity(userId, 0, 0, 1);
    }

    public void increaseTryCount() {
        totalTryCount++;
    }

    public void increaseSuccessCount() {
        successCount++;
    }

    public void increaseFailCount() {
        failCount++;
    }
}
