package com.game.subwayworkerserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false)
    private int exp;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int totalExp;

    @Column(nullable = false)
    private int coin;

    @Column(nullable = false)
    private int ticket;

    @Column(nullable = false)
    private int stage;

    public UserEntity(String name, String uuid, int exp, int level, int totalExp, int coin, int ticket, int stage) {
        this.name = name;
        this.uuid = uuid;
        this.exp = exp;
        this.level = level;
        this.totalExp = totalExp;
        this.coin = coin;
        this.ticket = ticket;
        this.stage = stage;
    }

    public static UserEntity of(final String username, final String uuid) {
        return new UserEntity(username, uuid, 0, 1, 0, 0, 0, 0);
    }
}
