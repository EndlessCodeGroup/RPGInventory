/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 osipf
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.item;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.utils.StringUtils;

/**
 * Created by OsipXD on 19.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemStat {
    private final StatType type;
    @NotNull
    private final OperationType operationType;
    private final boolean percentage;
    private final double minValue;
    private final double maxValue;

    ItemStat(StatType type, String value) {
        this.type = type;
        this.operationType = OperationType.valueOf(value.charAt(0));
        this.percentage = this.type.isOnlyPercentage() || value.endsWith("%");

        value = value.substring(1).replaceAll("%", "");
        if (value.contains("-")) {
            this.minValue = Double.parseDouble(value.split("-")[0]);
            this.maxValue = Double.parseDouble(value.split("-")[1]);
        } else {
            this.minValue = Double.parseDouble(value);
            this.maxValue = -1;
        }
    }

    @NotNull String getStringValue() {
        String value = this.operationType.getOperation() + StringUtils.doubleToString(this.minValue);

        if (this.maxValue != -1) {
            value += "-" + StringUtils.doubleToString(this.maxValue);
        }

        if (this.percentage) {
            value += "%";
        }

        return value;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return isRanged() ? this.maxValue : this.minValue;
    }

    private boolean isRanged() {
        return this.maxValue != -1;
    }

    public double getValue() {
        double value = this.minValue;

        if (maxValue != -1) {
            value += (this.maxValue - this.minValue) * Math.random();
        }

        return value;
    }

    boolean isPercentage() {
        return percentage;
    }

    @NotNull OperationType getOperationType() {
        return operationType;
    }

    public StatType getType() {
        return type;
    }


    enum OperationType {
        PLUS('+'),
        MINUS('-');

        private final char operation;

        OperationType(char operation) {
            this.operation = operation;
        }

        @NotNull
        public static OperationType valueOf(char operation) {
            for (OperationType operationType : OperationType.values()) {
                if (operationType.getOperation() == operation) {
                    return operationType;
                }
            }

            return PLUS;
        }

        public char getOperation() {
            return operation;
        }
    }

    @SuppressWarnings("unused")
    public enum StatType {
        DAMAGE,
        BOW_DAMAGE,
        HAND_DAMAGE,
        ARMOR,
        JUMP,
        CRIT_CHANCE(true),
        CRIT_DAMAGE(true),
        SPEED(true);

        private final boolean onlyPercentage;

        StatType() {
            this(false);
        }

        StatType(boolean onlyPercentage) {
            this.onlyPercentage = onlyPercentage;
        }

        public boolean isOnlyPercentage() {
            return this.onlyPercentage;
        }
    }
}
