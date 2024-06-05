package fi.livi.digitraffic.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    public void format() {
        Assertions.assertEquals("method=testMethod count=15 foo bar", StringUtil.format("method={} count={} {} bar", "testMethod", 15, "foo"));
        Assertions.assertEquals("foo bar", StringUtil.format("foo bar", "plaa"));
    }

}
