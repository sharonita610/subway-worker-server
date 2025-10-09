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

    private String name;
    private String uuid;

    private int level;
    private int exp;
    private int totalExp;

    private int coin;
    private int ticket;
    private int stage;

    public UserEntity(String name, String uuid, int level, int coin, int ticket, int stage) {
        this.name = name;
        this.uuid = uuid;
        this.level = level;
        this.coin = coin;
        this.ticket = ticket;
        this.stage = stage;
    }

    public static UserEntity of(final String username, final String uuid) {
    return new UserEntity(username, uuid, 1, 0, 0, 0);
    }
}
