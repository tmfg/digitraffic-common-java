package fi.livi.digitraffic.common.util;

import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.test.util.TestUtil;

public class ThreadUtilTest {

    @Test
    public void delayMs() {
        IntStream.range(0,4).parallel().forEach(i -> {
            final StopWatch duration = StopWatch.createStarted();
            final int delayMs = TestUtil.getRandom(500, 1500);
            ThreadUtil.delayMs(delayMs);
            AssertUtil.assertGe(duration.getTime(), delayMs, 500);
        });
    }
}
