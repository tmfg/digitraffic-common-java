package fi.livi.digitraffic.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathUtilTest {

    @Test
    public void floorToHalf() {
        Assertions.assertEquals(0, MathUtil.floorToHalf(0.0));
        Assertions.assertEquals(0.0, MathUtil.floorToHalf(0.1));
        Assertions.assertEquals(0.0, MathUtil.floorToHalf(0.499999999999));
        Assertions.assertEquals(0.5, MathUtil.floorToHalf(0.5));
        Assertions.assertEquals(0.5, MathUtil.floorToHalf(0.500000000001));
        Assertions.assertEquals(0.5, MathUtil.floorToHalf(0.999999999999));
        Assertions.assertEquals(1.0, MathUtil.floorToHalf(1.0));
        Assertions.assertEquals(1.0, MathUtil.floorToHalf(1.000000000001));
        Assertions.assertEquals(1.0, MathUtil.floorToHalf(1.499999999999));
        Assertions.assertEquals(1.5, MathUtil.floorToHalf(1.5));
        Assertions.assertEquals(1.5, MathUtil.floorToHalf(1.500000000001));
    }

    @Test
    public void ceilToHalf() {
        Assertions.assertEquals(0, MathUtil.ceilToHalf(0.0));
        Assertions.assertEquals(0.5, MathUtil.ceilToHalf(0.1));
        Assertions.assertEquals(0.5, MathUtil.ceilToHalf(0.499999999999));
        Assertions.assertEquals(0.5, MathUtil.ceilToHalf(0.5));
        Assertions.assertEquals(1.0, MathUtil.ceilToHalf(0.500000000001));
        Assertions.assertEquals(1.0, MathUtil.ceilToHalf(0.999999999999));
        Assertions.assertEquals(1.0, MathUtil.ceilToHalf(1.0));
        Assertions.assertEquals(1.5, MathUtil.ceilToHalf(1.000000000001));
        Assertions.assertEquals(1.5, MathUtil.ceilToHalf(1.499999999999));
        Assertions.assertEquals(1.5, MathUtil.ceilToHalf(1.5));
        Assertions.assertEquals(2.0, MathUtil.ceilToHalf(1.500000000001));
    }

    @Test
    public void roundToScale() {
        Assertions.assertEquals(1.1, MathUtil.roundToScale(1.1, 1));
        Assertions.assertEquals(1.1, MathUtil.roundToScale(1.11, 1));
        Assertions.assertEquals(0.9, MathUtil.roundToScale(0.9, 1));
        Assertions.assertEquals(1.0, MathUtil.roundToScale(0.9, 0));
        Assertions.assertEquals(2.0, MathUtil.roundToScale(1.5, 0));
        Assertions.assertEquals(1.11111, MathUtil.roundToScale(1.111111000, 5));
        Assertions.assertEquals(1.11112, MathUtil.roundToScale(1.111119000, 5));
    }
}
