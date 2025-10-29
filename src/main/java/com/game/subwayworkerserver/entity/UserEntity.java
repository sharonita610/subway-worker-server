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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private int exp;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int totalExp;

    @Column(nullable = false)
    private int coin;

    @Column(nullable = false)
    private int vacationTicket;

    @Column(nullable = false)
    private int gotchaTicket;

    @Column(nullable = false)
    private int stage;

    @Column(nullable = false)
    private boolean trapSuccess = false;

    @Column(nullable = false)
    private boolean  hasPendingReward = false;

    public UserEntity(String name, String uuid, int exp, int level, int totalExp, int coin, int vacationTicket,
                      int gotchaTicket, int stage, Gender gender) {
        this.name = name;
        this.uuid = uuid;
        this.exp = exp;
        this.level = level;
        this.totalExp = totalExp;
        this.coin = coin;
        this.vacationTicket = vacationTicket;
        this.gotchaTicket = gotchaTicket;
        this.stage = stage;
        this.gender = gender;
    }

    public static UserEntity of(final String username, final String uuid, Gender gender) {
        return new UserEntity(username, uuid, 0, 1, 0, 0, 0, 0, 0, gender);
    }

    public void updateRewards(int totalCoins, int totalTickets, int totalExperience) {
        this.coin += totalCoins;
        this.vacationTicket += totalTickets;
        this.exp += totalExperience;
        this.totalExp += totalExperience;
    }

    public void increaseGotchaTicket() {
        this.gotchaTicket++;
    }

    public void updateTrapSuccess() {
        this.trapSuccess = true;
    }

    public boolean isTrapSuccess() {
        return this.trapSuccess;
    }
}
