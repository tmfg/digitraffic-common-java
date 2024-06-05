package fi.livi.digitraffic.common.util;

import org.slf4j.helpers.MessageFormatter;

public class StringUtil {

    /**
     * Performs string formation with log style messagePattern.
     * For example,
     *  format("method={} count={}.", "myMethod", 10);
     * @param messagePattern Pattern to be used.
     * @param parameters The arguments to be substituted in place of the formatting anchors.
     * @return Formatted string.
     */
    public static String format(final String messagePattern, final Object... parameters) {
        return MessageFormatter.arrayFormat(messagePattern, parameters).getMessage();
    }
}
