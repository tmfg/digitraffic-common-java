package fi.livi.digitraffic.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class StringUtil {
    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

    private static final ObjectWriter jsonObjectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

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


    public static String toJsonString(final Object o) {
        try {
            return jsonObjectWriter.writeValueAsString(o);
        } catch (final JsonProcessingException e) {
            log.error("Failed to convert object to JSON-string", e);
        }
        return o.toString();
    }

    public static String toJsonStringLogSafe(final Object o) {
        if (o == null) {
            return null;
        }
        try {
            return padKeyValuePairsEqualitySignWithSpaces(jsonObjectWriter.writeValueAsString(o));
        } catch (final JsonProcessingException e) {
            log.error("Failed to convert object to JSON-string", e);
            return padKeyValuePairsEqualitySignWithSpaces(o.toString());
        }
    }

    public static String padKeyValuePairsEqualitySignWithSpaces(final String value) {
        if (value != null) {
            return value.replace("=", " = ");
        }
        return null;
    }

    /**
     * Replaces given str with changeToStr if searchStr is found in str.
     * @param str string to search from
     * @param searchStr string to search
     * @param changeToStr string to return if searchStr is found
     * @return either original str of changeToStr if match is found.
     */
    public static String changeToIfContains(final String str, final String searchStr, final String changeToStr) {
        if (StringUtils.contains(str, searchStr)) {
            return changeToStr;
        }
        return str;
    }
}