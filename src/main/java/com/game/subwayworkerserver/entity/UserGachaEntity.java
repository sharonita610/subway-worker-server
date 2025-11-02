package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Entity
@Table(name = "user_gacha")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGachaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GachaTreasure obtainedTreasure;

    @Min(0)
    @Max(20)
    @Column(nullable = false)
    private int gachaCount;

    @Builder
    public UserGachaEntity(long userId, GachaTreasure obtainedTreasure, int gachaCount) {
        this.userId = userId;
        this.obtainedTreasure = obtainedTreasure;
        this.gachaCount = gachaCount;
    }

    public static List<UserGachaEntity> from(final long userId) {
        return Arrays.stream(GachaTreasure.values())
                .map(treasure -> UserGachaEntity.builder()
                        .userId(userId)
                        .obtainedTreasure(treasure)
                        .gachaCount(0) // 초기 사용 횟수 0
                        .build())
                .toList();
    }
}
