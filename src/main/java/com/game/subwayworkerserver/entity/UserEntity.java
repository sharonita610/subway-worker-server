package com.game.subwayworkerserver.entity;

import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

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
    private int gachaTicket;

    @Column(nullable = false)
    private int stage;

    @Column(nullable = false)
    private boolean trapSuccess = false;

    @Column(nullable = false)
    private boolean hasPendingReward = false;

    @Column(nullable = false)
    private boolean hasPendingMail = false;

    @Column(nullable = false)
    private boolean hasDailyCheckInReward = false;

    public UserEntity(String name, String uuid, int exp, int level, int totalExp, int coin, int vacationTicket,
                      int gachaTicket, int stage, Gender gender) {
        this.name = name;
        this.uuid = uuid;
        this.exp = exp;
        this.level = level;
        this.totalExp = totalExp;
        this.coin = coin;
        this.vacationTicket = vacationTicket;
        this.gachaTicket = gachaTicket;
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

    public void increaseGachaTicket() {
        this.gachaTicket++;
    }

    public void updateTrapSuccess() {
        this.trapSuccess = true;
    }

    public void updateTrapDone() {
        this.trapSuccess = false;
    }

    public boolean hasCoinMoreThanItemPrice(final int itemPrice) {
        return this.coin >= itemPrice;
    }

    public void purchaseWithCoin(final int itemPrice) {
        this.coin -= itemPrice;
    }

    public boolean hasEnoughVacationTickets(final int itemPrice) {
        return this.vacationTicket >= itemPrice;
    }

    public void purchaseWithVacationTickets(final int acquiredCoin, final int itemPrice) {
        this.coin += acquiredCoin;
        this.vacationTicket -= itemPrice;
    }

    public void increaseVacationTicket(final int acquiredTickets) {
        this.vacationTicket += acquiredTickets;
    }

    public void purchaseWithVacationTickets(final int price) {
        if (this.vacationTicket < price) {
            throw new BaseException(BAD_REQUEST, "휴가 티켓이 부족하여 구매할 수 없습니다.");
        }
        this.vacationTicket -= price;
    }

    public boolean hasEnoughTickets(final String currencyType, final int itemPrice) {
        if (currencyType.equals("vacationTicket")) {
            return this.vacationTicket >= itemPrice;
        } else {
            return this.gachaTicket >= itemPrice;
        }
    }

    public void purchaseWithTickets(final String currencyType, final int itemPrice) {
        if (currencyType.equals("vacationTicket")) {
            this.vacationTicket -= itemPrice;
        } else {
            this.gachaTicket -= itemPrice;
        }
    }

    public void increaseCoin(final int quantity) {
        this.coin += quantity;
    }

    public void checkAndPurchase(final int totalCost) {
        if (this.coin < totalCost) {
            throw new BaseException(BAD_REQUEST, "코인이 부족해요!");
        }
        this.coin -= totalCost;
    }

    public void grantReward(int exp, int extraExp, int coin) {
        this.exp += exp + extraExp;
        this.totalExp += exp + extraExp;
        this.coin += coin;
    }

    public void updateLevelAndExp(int currentLevel, int currentExp) {
        this.level += currentLevel;
        this.exp += currentExp;
    }

    public void hasReward() {
        hasPendingReward = true;
    }

    public void claimRewardDone() {
        hasPendingReward = false;
    }

    public void giveReward(final RewardType type, final int quantity) {
        if (quantity <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "지급 수량은 1 이상이어야 합니다.");
        }

        switch (type) {
            case COIN:
                this.coin += quantity;
                break;
            case VACATION_TICKET:
                this.vacationTicket += quantity;
                break;
            case GACHA_TICKET:
                this.gachaTicket += quantity;
                break;
            default:
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "알 수 없는 보상 타입입니다: " + type);
        }
    }
}
