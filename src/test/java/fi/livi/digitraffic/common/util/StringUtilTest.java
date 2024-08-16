package fi.livi.digitraffic.common.util;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StringUtilTest {

    @Test
    public void format() {
        Assertions.assertEquals("method=testMethod count=15 foo bar", StringUtil.format("method={} count={} {} bar", "testMethod", 15, "foo"));
        Assertions.assertEquals("foo bar", StringUtil.format("foo bar", "plaa"));
    }

    @Test
    public void toJsonStringAndToJsonStringLogSafe() {
        final int number = (int) (Math.random() * 10);
        final String text = UUID.randomUUID().toString();
        final MyObject object = new MyObject(text, number);
        final String expected =
            "{\n" +
            "  \"text\" : \"" + text + "\",\n" +
            "  \"number\" : " + number + "\n" +
            "}";
        Assertions.assertEquals(expected, StringUtil.toJsonString(object));
        Assertions.assertEquals(expected, StringUtil.toJsonStringLogSafe(object));
    }

    @Test
    public void toJsonStringLogSafe() {
        final int number = (int) (Math.random() * 10);
        final String text = "value=" + UUID.randomUUID();
        final MyObject object = new MyObject(text, number);
        final String expected =
            "{\n" +
                "  \"text\" : \"" + text.replace("=", " = ") + "\",\n" +
                "  \"number\" : " + number + "\n" +
                "}";
        Assertions.assertEquals(expected, StringUtil.toJsonStringLogSafe(object));
    }

    @Test
    public void padKeyValuePairsEqualitySignWithSpaces() {
        final String text = "a=b, c=d, e = f";
        final String expected = "a = b, c = d, e  =  f";
        Assertions.assertEquals(expected, StringUtil.padKeyValuePairsEqualitySignWithSpaces(text));
    }

    static class MyObject {
        @JsonProperty
        private final String text;
        @JsonProperty
        private final Integer number;

        public MyObject(final String text, final Integer number) {
            this.text = text;
            this.number = number;
        }
    }
}
