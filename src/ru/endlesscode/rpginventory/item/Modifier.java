package ru.endlesscode.rpginventory.item;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.utils.Utils;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Modifier {
    public static final Modifier EMPTY = new Modifier(0, 1);

    private final double minBonus;
    private final double maxBonus;
    private final double minMultiplier;
    private final double maxMultiplier;

    private Modifier(double bonus, float multiplier) {
        this(bonus, bonus, multiplier, multiplier);
    }

    Modifier(double minBonus, double maxBonus, double minMultiplier, double maxMultiplier) {
        if (minBonus > maxBonus) {
            minBonus += maxBonus;
            maxBonus = minBonus - maxBonus;
            minBonus -= maxBonus;
        }

        this.minBonus = minBonus;
        this.maxBonus = maxBonus;

        if (minMultiplier <= 0) {
            minMultiplier = 0.1;
        }

        if (maxMultiplier <= 0) {
            maxMultiplier = 0.1;
        }

        if (minMultiplier > maxMultiplier) {
            minMultiplier += maxMultiplier;
            maxMultiplier = minMultiplier - maxMultiplier;
            minMultiplier -= maxMultiplier;
        }

        this.minMultiplier = Utils.round(minMultiplier, 3);
        this.maxMultiplier = Utils.round(maxMultiplier, 3);
    }

    private double getMinBonus() {
        return minBonus;
    }

    private double getMaxBonus() {
        return maxBonus;
    }

    private double getMinMultiplier() {
        return minMultiplier;
    }

    private double getMaxMultiplier() {
        return maxMultiplier;
    }

    public double getBonus() {
        return Utils.round(this.minBonus + (this.maxBonus - this.minBonus) * Math.random(), 1);
    }

    public double getMultiplier() {
        return Utils.round(this.minMultiplier + (this.maxMultiplier - this.minMultiplier) * Math.random(), 1);
    }

    @Override
    public String toString() {
        if (this.equals(EMPTY)) {
            return RPGInventory.getLanguage().getCaption("stat.message.no_bonus");
        }

        String str = "";

        if (this.minBonus != 0 && this.maxBonus != 0) {
            double minBonus = this.minBonus;
            double maxBonus = this.maxBonus;

            if ((maxBonus <= 0 || minBonus >= 0) && Math.abs(minBonus) > Math.abs(maxBonus)) {
                minBonus += maxBonus;
                maxBonus = minBonus - maxBonus;
                minBonus -= maxBonus;
            }

            if (minBonus >= 0) {
                str += "+";
            }
            str += minBonus;

            if (minBonus != maxBonus) {
                str += "-" + (minBonus * maxBonus >= 0 ? Double.valueOf(Math.abs(maxBonus)) : "(+" + maxBonus + ")");
            }
        }

        if (this.minMultiplier != 1 && this.maxMultiplier != 1) {
            double minMultiplier = Utils.round(this.minMultiplier * 100 - 100, 1);
            double maxMultiplier = Utils.round(this.maxMultiplier * 100 - 100, 1);

            if ((maxMultiplier <= 0 || minMultiplier >= 0) && Math.abs(minMultiplier) > Math.abs(maxMultiplier)) {
                minMultiplier += maxMultiplier;
                maxMultiplier = minMultiplier - maxMultiplier;
                minMultiplier -= maxMultiplier;
            }

            if (minMultiplier >= 0) {
                str += "+";
            }
            str += minMultiplier;

            if (minMultiplier != maxMultiplier) {
                str += "-" + (minMultiplier * maxMultiplier >= 0 ? Double.valueOf(Math.abs(maxMultiplier)) : "(+" + maxMultiplier + ")");
            }
            str += "%";
        }

        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Modifier)) {
            return false;
        }

        Modifier other = (Modifier) obj;
        return other.getMinBonus() == this.minBonus && other.getMinMultiplier() == this.minMultiplier &&
               other.getMaxBonus() == this.maxBonus && other.getMaxMultiplier() == this.maxMultiplier;
    }
}
