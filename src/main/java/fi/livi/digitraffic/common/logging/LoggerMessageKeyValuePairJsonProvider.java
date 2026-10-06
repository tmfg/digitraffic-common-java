package fi.livi.digitraffic.common.logging;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.AbstractJsonProvider;

/**
 * Provider to log key=value -pairs in messages as json key-values.
 */
public class LoggerMessageKeyValuePairJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    // Finds key=value pairs in the message.
    // A value in quotes may contain spaces and ends only at a quote that is not preceded by a
    // backslash. Without quotes the value ends at the next space.
    private final static Pattern keyValuePattern = Pattern.compile("([^\\s=]+)=(\"(?:\\\\[\\s\\S]|[^\"\\\\])*\"|\\S*)");
    // Must start with upper or lower case letter
    // Must end with number or upper or lower case letter
    // Between can be numbers, letters, one at the time of "_", "-" or "." surrounded by numbers or letters
    // The "*+" never gives back what it has matched, which keeps a long non-matching key from being slow
    private final static Pattern keyPattern = Pattern.compile("^[a-zA-Z](?:[-._]?[a-zA-Z0-9]+)*+$");

    @Override
    public void writeTo(final JsonGenerator generator, final ILoggingEvent event) {
        final String formattedMessage = event.getFormattedMessage();

        if (StringUtils.isBlank(formattedMessage)) {
            return;
        }

        final List<Pair<String, String>> kvPairs = parseKeyValuePairs(formattedMessage);

        if (kvPairs.isEmpty()) {
            return;
        }

        final Set<String> hasWrittenFieldNames = new HashSet<>();
        kvPairs.forEach(e -> {
            if (!hasWrittenFieldNames.contains(e.getKey())) {
                try {
                    final Object objectValue = getObjectValue(e.getValue());
                    generator.writeName(e.getKey());
                    generator.writePOJO(objectValue);
                    hasWrittenFieldNames.add(e.getKey());
                } catch (final JacksonException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private static Object getObjectValue(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // Quoted values are always treated as one string, quotes are removed and escapes undone
        if(isQuoted(value)) {
            return unescape(value.substring(1, value.length() - 1));
        } else if( "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ) {
            return Boolean.valueOf(value);
        }

        try {
            // Iso date time value
            return ZonedDateTime.parse(value).toInstant().toString();
        } catch (final DateTimeParseException e) {
            // empty
        }
        // A number has at most one separator. More than that is an IP address, a version number or
        // similar, so keep it as a string instead of parsing a part of it.
        if (separatorCount(value) > 1) {
            return value;
        }
        // NumberFormat also accepts digits from other writing systems, so "١٢٣" would become 123.
        if (hasNonAsciiDigit(value)) {
            return value;
        }
        // A single comma is a decimal separator. NumberFormat would read it as grouping and turn
        // "0,003" into 3. Comma separated lists are logged quoted and never get here.
        final boolean commaIsDecimalSeparator = value.indexOf(',') >= 0;
        final NumberFormat format = commaIsDecimalSeparator
                ? commaDecimalFormat()
                : NumberFormat.getInstance(Locale.ROOT);
        // Parsing stops at the first character that does not fit, so "123abc" would become 123.
        // Only use the result when the whole value was a number.
        final ParsePosition position = new ParsePosition(0);
        final Number number = format.parse(value, position);
        if (number == null || position.getIndex() < value.length()) {
            return value;
        }
        // NumberFormat returns a Long when there is no fraction, so "2.0" would be written as 2.
        // OpenSearch locks the field type from the first document, which would then truncate every
        // later decimal to an integer. Keep decimals as Double.
        return hasFractionalPart(value, commaIsDecimalSeparator ? ',' : '.') ? number.doubleValue() : number;
    }

    /** Number of {@code .} and {@code ,} characters in the value. */
    private static int separatorCount(final String value) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '.' || c == ',') {
                count++;
            }
        }
        return count;
    }

    /** Whether the value has a digit outside {@code 0-9}. */
    private static boolean hasNonAsciiDigit(final String value) {
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (Character.isDigit(c) && (c < '0' || c > '9')) {
                return true;
            }
        }
        return false;
    }

    /** Parses with {@code ,} as the decimal separator. */
    private static DecimalFormat commaDecimalFormat() {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setDecimalSeparator(',');
        final DecimalFormat format = new DecimalFormat("0.#", symbols);
        format.setGroupingUsed(false);
        return format;
    }

    /** Whether the value has a fractional part ({@code 0.5}, {@code .5}). */
    private static boolean hasFractionalPart(final String value, final char separator) {
        final int index = value.indexOf(separator);
        final boolean digitFollows =
                index >= 0 && index + 1 < value.length() && Character.isDigit(value.charAt(index + 1));
        return digitFollows && (index == 0 || Character.isDigit(value.charAt(index - 1)));
    }

    private static boolean isQuoted(final String value) {
        return value != null && value.length() > 2 && value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"';
    }

    /** Turns {@code \"} back into {@code "} and {@code \\} back into {@code \}. */
    private static String unescape(final String value) {
        if (value.indexOf('\\') < 0) {
            return value;
        }
        final StringBuilder unescaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                final char next = value.charAt(i + 1);
                if (next == '\"' || next == '\\') {
                    unescaped.append(next);
                    i++;
                    continue;
                }
            }
            unescaped.append(c);
        }
        return unescaped.toString();
    }

    private static List<Pair<String, String>> parseKeyValuePairs(final String formattedMessage) {
        final String message = stripXmlTags(formattedMessage);
        final List<Pair<String, String>> kvPairs = new ArrayList<>();
        final Matcher matcher = keyValuePattern.matcher(message);
        while (matcher.find()) {
            final String key = matcher.group(1);
            final String value = valueUpToNextKey(matcher.group(2));
            // A lone quote is left over from a value whose closing quote is missing, so there is no value to log
            if (StringUtils.isNotBlank(value) && !"\"".equals(value) && keyPattern.matcher(key).matches()) {
                kvPairs.add(Pair.of(key, safeValue(value)));
            }
        }
        return kvPairs;
    }

    /** Cuts an unquoted value at the next {@code =}, so "a=b=c" gives "b". Quoted values are kept whole. */
    private static String valueUpToNextKey(final String value) {
        if (isQuoted(value)) {
            return value;
        }
        final int index = value.indexOf('=');
        return index < 0 ? value : value.substring(0, index);
    }

    private static String safeValue(final String value) {
        if("NULL".equalsIgnoreCase(value)) return null;

        return value;
    }

    /** Index of the closing quote of a value starting at {@code start}, or -1. An escaped quote does not close it. */
    private static int endOfQuotedValue(final String message, final int start) {
        for (int i = start + 1; i < message.length(); i++) {
            final char c = message.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\"') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes {@code <…>} tags, but keeps quoted values as they are so that a value can contain tags of its
     * own. Tags are skipped whole, so an attribute inside a tag is not mistaken for a quoted value.
     */
    private static String stripXmlTags(final String message) {
        if (message.contains("healthCheckValue=")) {
            // Can be ie. healthCheckValue=<status>ok</status>
            return message;
        }
        final StringBuilder stripped = new StringBuilder(message.length());
        int i = 0;
        while (i < message.length()) {
            final char c = message.charAt(i);
            if (c == '=' && i + 1 < message.length() && message.charAt(i + 1) == '\"' && isKeyBefore(message, i)) {
                final int end = endOfQuotedValue(message, i + 1);
                if (end > 0) {
                    stripped.append(message, i, end + 1);
                    i = end + 1;
                    continue;
                }
            }
            if (c == '<') {
                final int end = message.indexOf('>', i);
                if (end > 0) {
                    stripped.append(' ');
                    i = end + 1;
                    continue;
                }
            }
            stripped.append(c);
            i++;
        }
        return stripped.toString();
    }

    /**
     * Tells whether the text right before {@code equalsIndex} is a key that would be logged. Without this a
     * quoted value would also be protected for something that is not a key at all, and tags inside it would
     * be left to become fields of their own.
     */
    private static boolean isKeyBefore(final String message, final int equalsIndex) {
        int start = equalsIndex;
        while (start > 0 && !Character.isWhitespace(message.charAt(start - 1)) && message.charAt(start - 1) != '=') {
            start--;
        }
        return start < equalsIndex && keyPattern.matcher(message.substring(start, equalsIndex)).matches();
    }
}