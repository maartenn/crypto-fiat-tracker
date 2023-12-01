package com.naberink.crypto.cryptofiattracker;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {
    public static final BigDecimal SATS_IN_BITCOIN = BigDecimal.valueOf(100_000_000);

    /**
     * Determines the nearest epoch to the given epoch, preferring the floor if
     * equal distance.
     *
     * @param epoch      The target epoch.
     * @param floorKey   The floor key from the map.
     * @param ceilingKey The ceiling key from the map.
     * @return The nearest epoch.
     */
    public static Long getNearestEpoch(long epoch, Long floorKey, Long ceilingKey) {
        if (floorKey == null) {
            return ceilingKey;
        }
        if (ceilingKey == null) {
            return floorKey;
        }

        long floorDiff = epoch - floorKey;
        long ceilingDiff = ceilingKey - epoch;

        // Prefer the floor key if it's closer or equal distance to the ceiling key
        return (floorDiff <= ceilingDiff) ? floorKey : ceilingKey;
    }

    public static BigDecimal calculateFiatPrice(long sats, BigDecimal price) {
        return new BigDecimal(sats)
                .divide(SATS_IN_BITCOIN)
                .multiply(price)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
