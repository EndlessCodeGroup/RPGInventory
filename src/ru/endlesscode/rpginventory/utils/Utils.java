package ru.endlesscode.rpginventory.utils;

import java.math.BigDecimal;

/**
 * Created by OsipXD on 20.05.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Utils {
    public static double round(double a, int scale) {
        return new BigDecimal(a).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
