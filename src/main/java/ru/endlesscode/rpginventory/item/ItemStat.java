package ru.endlesscode.rpginventory.item;

import ru.endlesscode.rpginventory.utils.StringUtils;

/**
 * Created by OsipXD on 19.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemStat {
    private final StatType type;
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

    String getStringValue() {
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
        return maxValue;
    }

    public boolean isRanged() {
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

    OperationType getOperationType() {
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
        HEALTH,
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
