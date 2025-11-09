package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "user_subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSubscriptionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionItem type;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = true)
    private LocalDate lastRewardClaimedDate;

    @Builder
    public UserSubscriptionEntity(Long userId, SubscriptionItem type, LocalDate expirationDate) {
        this.userId = userId;
        this.type = type;
        this.expirationDate = expirationDate;
    }

    public void extendSubscription(int daysToExtend) {
        LocalDate today = LocalDate.now();

        LocalDate startPoint = this.expirationDate;

        // 만료일이 오늘 날짜보다 이전이라면, 오늘부터 시작합니다.
        if (startPoint.isBefore(today)) {
            startPoint = today;
        }

        // 기존 만료일로부터 기간 연장
        this.expirationDate = startPoint.plusDays(daysToExtend);
    }

    public boolean canClaimDailyReward(LocalDate today) {
        // 1. 구독이 만료되었는지 확인
        if (this.expirationDate.isBefore(today)) {
            return false;
        }

        // 2. 오늘 이미 보상을 받았는지 확인
        // lastRewardClaimedDate가 null이 아니면서 오늘 날짜와 같으면 이미 수령 완료
        if (this.lastRewardClaimedDate != null && this.lastRewardClaimedDate.isEqual(today)) {
            return false;
        }

        return true;
    }

    public void markDailyRewardClaimed(LocalDate today) {
        this.lastRewardClaimedDate = today;
    }
}
