package fi.livi.digitraffic.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class MathUtil {

    protected MathUtil() {
        throw new AssertionError("The " + getClass().getSimpleName() + " class methods should be accessed statically");
    }

    public static double floorToHalf(final double number) {
        return Math.floor(number * 2.0) / 2.0;
    }

    public static double ceilToHalf(final double number) {
        return Math.ceil(number * 2.0) / 2.0;
    }

    /**
     * Rounds no nearest half using given scale
     * @param number to round
     * @param scale scale to round to 
     * @return rounded value
     * 
     * @see BigDecimal#setScale(int, RoundingMode)
     */
    public static double roundToScale(final double number, final int scale) {
        return BigDecimal.valueOf(number).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

}
