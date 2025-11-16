package com.game.subwayworkerserver.service.dto;

public record CoworkerUpgradeVO (
        int basePoint,
        int newExp
){
    public static CoworkerUpgradeVO of(double baseExpPoint, int expToUpgrade) {
        return new CoworkerUpgradeVO((int)baseExpPoint, expToUpgrade);
    }
}
