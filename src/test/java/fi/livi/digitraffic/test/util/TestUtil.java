package fi.livi.digitraffic.test.util;

import java.util.Random;

public class TestUtil {

    public static int getRandom(final int minInclusive, final int maxExclusive) {
        final Random random = new Random();
        return random.ints(minInclusive, maxExclusive).findFirst().orElseThrow();
    }
}
