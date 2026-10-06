package fi.livi.digitraffic.common.logging;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Builds the {@code key=value} log message that {@link LoggerMessageKeyValuePairJsonProvider} turns into
 * JSON fields.
 *
 * <p>Field names follow the OpenTelemetry semantic conventions, which keep the unit out of the name:
 * a duration is in seconds and a size is in bytes. Use {@code duration=1.4}, not {@code duration_ms=1400}.
 */
public final class LogFields {

    // A quote or a backslash in the value must be escaped so the value is not cut short
    private static final Pattern NEEDS_ESCAPE_IN_LOG_VALUE = Pattern.compile("([\\\\\"])");
    // Newlines would split the log line, so they are turned into spaces
    private static final Pattern NEWLINE = Pattern.compile("\\R+");
    // Any of these in an unquoted value would confuse the provider: whitespace starts the next field,
    // '=' starts a new key, and a quote or backslash needs the escaping that only quoted() applies
    private static final Pattern NEEDS_QUOTING = Pattern.compile("[\\s=\"\\\\]");

    /** Absent values are spelled out, because the provider drops a blank one and reads this back as a JSON null. */
    private static final String ABSENT = "NULL";

    private LogFields() {
    }

    /**
     * Renders a wide event as one space separated {@code key=value} message. Logging the map itself would
     * produce {@code Map.toString()}, which the provider cannot parse.
     *
     * <p>A string value that would confuse the provider unquoted - one with a space, an {@code =}, a quote,
     * a backslash, or the literal text {@code NULL} - is quoted and escaped automatically, the same way
     * {@link #quoted(String)} does it. A plain string, a number, or a boolean is written as is.
     */
    public static String of(final Map<String, Object> event) {
        return event.entrySet().stream()
                .map(field -> field.getKey() + "=" + value(field.getValue()))
                .collect(Collectors.joining(" "));
    }

    /**
     * Converts a millisecond duration to the seconds that OpenTelemetry expects of a {@code duration} field.
     *
     * <p>The result is a double on purpose. OpenSearch locks a field to the type of the first document it
     * sees, so an integral {@code 0} would map the field as a long and silently truncate every later decimal.
     * A double always renders with a decimal point, and in every locale.
     */
    public static double durationSeconds(final long millis) {
        return millis / 1000.0;
    }

    /** As {@link #durationSeconds(long)}, for a {@link Duration}. */
    public static double durationSeconds(final Duration duration) {
        return duration == null ? 0.0 : durationSeconds(duration.toMillis());
    }

    /**
     * Quotes a value that may contain spaces, so the provider keeps all of it instead of ending the value at
     * the first space. Newlines become spaces and quotes are escaped. A blank value becomes {@code NULL}.
     */
    public static String quoted(final String value) {
        if (value == null || value.isBlank()) {
            return ABSENT;
        }
        final String singleLine = NEWLINE.matcher(value).replaceAll(" ").trim();
        // "\\\\$1" is a backslash and the matched character: the compiler reads it as \\$1 and replaceAll
        // reads \\ as one backslash
        return "\"" + NEEDS_ESCAPE_IN_LOG_VALUE.matcher(singleLine).replaceAll("\\\\$1") + "\"";
    }

    private static String value(final Object object) {
        switch (object) {
            case null -> {
                return ABSENT;
            }
            // NaN round-trips as a JSON number, but Infinity does not - the provider reads it back as a
            // String. Either way neither is a measurement, so both are reported as absent rather than
            // risking a field whose type flips between runs.
            case final Double d when !Double.isFinite(d) -> {
                return ABSENT;
            }
            case final Float f when !Float.isFinite(f) -> {
                return ABSENT;
            }
            default -> {
            }
        }
        final String text = String.valueOf(object);
        if (text.isBlank()) {
            return ABSENT;
        }
        // Only a String needs the quoting check: a number or a boolean cannot contain the characters
        // that confuse the provider, and quoting one would turn it into a JSON string.
        if (object instanceof CharSequence && (NEEDS_QUOTING.matcher(text).find() || ABSENT.equalsIgnoreCase(text))) {
            return quoted(text);
        }
        return text;
    }
}
