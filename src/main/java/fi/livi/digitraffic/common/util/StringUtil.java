package fi.livi.digitraffic.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

public class StringUtil {
    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

    private static final ObjectWriter jsonObjectWriter = JsonMapper.builder().build().writerWithDefaultPrettyPrinter();

    /**
     * Performs string formation with log style messagePattern.
     * For example,
     * format("method={} count={}.", "myMethod", 10);
     *
     * @param messagePattern Pattern to be used.
     * @param parameters     The arguments to be substituted in place of the formatting anchors.
     * @return Formatted string.
     */
    public static String format(final String messagePattern, final Object... parameters) {
        return MessageFormatter.arrayFormat(messagePattern, parameters).getMessage();
    }


    public static String toJsonString(final Object o) {
        try {
            return jsonObjectWriter.writeValueAsString(o);
        } catch (final JacksonException e) {
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
        } catch (final JacksonException e) {
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
     * Returns the last path segment of a filename (the part after the last {@code /}).
     * If the string contains no {@code /}, the original value is returned unchanged.
     * If {@code filename} ends with {@code /}, an empty string is returned.
     *
     * <pre>{@code
     *   fileBaseName("https://example.com/4.6.zip!locations.csv") // "4.6.zip!locations.csv"
     *   fileBaseName("locations.csv")                             // "locations.csv"
     *   fileBaseName("ends/with/slash/")                          // ""
     *   fileBaseName(null)                                        // null
     * }</pre>
     *
     * @param filename the path string to extract the base name from; may be {@code null}
     * @return the base name (substring after the last {@code /}), the original string if no
     * {@code /} is present, or {@code null} if {@code filename} is {@code null}
     */
    public static String fileBaseName(final String filename) {
        if (filename == null) {
            return null;
        }
        final int idx = filename.lastIndexOf('/');
        return idx >= 0 ? filename.substring(idx + 1) : filename;
    }

    /**
     * Returns {@code changeToStr} if {@code str} contains {@code searchStr}; otherwise returns
     * {@code str} unchanged. If either {@code str} or {@code searchStr} is {@code null}, {@code str}
     * is returned unchanged.
     *
     * @param str         the string to search within; may be {@code null}
     * @param searchStr   the substring to look for; may be {@code null}
     * @param changeToStr the value to return when {@code searchStr} is found; may be {@code null}
     * @return {@code changeToStr} if {@code searchStr} is found in {@code str}, otherwise {@code str}
     */
    public static String changeToIfContains(final String str, final String searchStr, final String changeToStr) {
        if (str != null && searchStr != null && str.contains(searchStr)) {
            return changeToStr;
        }
        return str;
    }
}