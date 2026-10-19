package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.item.ItemName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_trap_map")
public class UserTrapMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemName trapItemName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isSuccessful = false;

    @Column(nullable = false)
    private boolean isTrapped = false;

    private String capturedCoworkerId;

    @Builder
    public UserTrapMapEntity(Long userId, ItemName trapItemName, double latitude, double longitude, LocalDateTime expiresAt) {
        this.userId = userId;
        this.trapItemName = trapItemName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.expiresAt = expiresAt;
    }

    public static UserTrapMapEntity of(Long userId, ItemName trapItemName, double lat, double lon, LocalDateTime expiresAt) {
        return UserTrapMapEntity.builder()
                .userId(userId)
                .trapItemName(trapItemName)
                .latitude(lat)
                .longitude(lon)
                .expiresAt(expiresAt)
                .build();
    }

    public void markSuccess(String capturedCoworkerId) {
        this.isTrapped = true;
        this.capturedCoworkerId = capturedCoworkerId;
    }

    public void setIsSuccessful() {
        this.isSuccessful = true;
        this.isTrapped = false;
    }

    public void setExpiresAt() {
        this.expiresAt = LocalDateTime.now();
    }

    public void setIsNotSuccessful() {
        this.isSuccessful = false;
    }
}
