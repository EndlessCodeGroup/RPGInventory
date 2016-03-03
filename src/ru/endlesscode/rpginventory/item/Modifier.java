package ru.endlesscode.rpginventory.item;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Modifier {
    private final double bonus;
    private final float multiplier;

    public Modifier(double bonus, float multiplier) {
        this.bonus = bonus;
        this.multiplier = multiplier;
    }

    public double getBonus() {
        return bonus;
    }

    public float getMultiplier() {
        return multiplier;
    }
}
